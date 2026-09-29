#!/usr/bin/env python3
"""Finds Mach-O binaries in an app bundle and inside jar files.

    macho.py files <dir>                  one line "<exe|lib> <path>" per binary
    macho.py jar-extract <jar> <dest>     extract Mach-O entries to <dest>,
                                          print their names
    macho.py jar-update <jar> <src>       replace every entry that exists as a
                                          file under <src>

Apple's notary service also inspects jars and rejects unsigned binaries inside
them, such as the .dylib files in adaptagrams.jar. Detection uses the file
header, not the extension. Standard library only.
"""
import os
import struct
import sys
import zipfile

MH_EXECUTE = 0x2

# Magic numbers read big-endian; little-endian thin binaries (Intel and Apple
# silicon) therefore appear as *_CIGAM.
THIN = {
    0xFEEDFACE: ">", 0xFEEDFACF: ">",   # MH_MAGIC, MH_MAGIC_64
    0xCEFAEDFE: "<", 0xCFFAEDFE: "<",   # MH_CIGAM, MH_CIGAM_64
}
FAT = 0xCAFEBABE      # same magic as Java class files
FAT_64 = 0xCAFEBABF


def macho_kind(read_at):
    """'exe', 'lib' or None. read_at(offset, n) returns bytes of the file."""
    head = read_at(0, 8)
    if len(head) < 8:
        return None
    magic, second = struct.unpack(">II", head)
    if magic in THIN:
        return _thin_kind(read_at, 0, THIN[magic])
    if magic in (FAT, FAT_64):
        # Class files have minor/major version here (major >= 45); universal
        # binaries have a handful of architectures.
        if not 0 < second < 20:
            return None
        width = 4 if magic == FAT else 8
        raw = read_at(16, width)
        if len(raw) < width:
            return None
        offset = struct.unpack(">I" if magic == FAT else ">Q", raw)[0]
        inner = read_at(offset, 4)
        if len(inner) < 4 or struct.unpack(">I", inner)[0] not in THIN:
            return None
        return _thin_kind(read_at, offset, THIN[struct.unpack(">I", inner)[0]])
    return None


def _thin_kind(read_at, offset, endian):
    raw = read_at(offset + 12, 4)
    if len(raw) < 4:        # truncated file with a matching header
        return None
    filetype = struct.unpack(endian + "I", raw)[0]
    return "exe" if filetype == MH_EXECUTE else "lib"


def file_reader(path):
    def read_at(offset, n):
        with open(path, "rb") as f:
            f.seek(offset)
            return f.read(n)
    return read_at


def bytes_reader(data):
    return lambda offset, n: data[offset:offset + n]


def cmd_files(root):
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames.sort()
        for name in sorted(filenames):
            path = os.path.join(dirpath, name)
            if os.path.islink(path) or name.endswith(".class"):
                continue
            kind = macho_kind(file_reader(path))
            if kind:
                print(kind, path)


def macho_entries(jar):
    """Names of Mach-O entries; only entries with a matching header are read fully."""
    with zipfile.ZipFile(jar) as z:
        for info in z.infolist():
            name = info.filename
            if name.endswith("/") or name.endswith(".class"):
                continue
            with z.open(info) as f:
                head = f.read(4)
            if len(head) < 4:
                continue
            magic = struct.unpack(">I", head)[0]
            if magic not in THIN and magic not in (FAT, FAT_64):
                continue
            if macho_kind(bytes_reader(z.read(info))):
                yield name


def cmd_jar_extract(jar, dest):
    names = list(macho_entries(jar))
    with zipfile.ZipFile(jar) as z:
        for name in names:
            target = os.path.join(dest, name)
            if not os.path.realpath(target).startswith(os.path.realpath(dest) + os.sep):
                sys.exit("unsafe entry name: " + name)
            os.makedirs(os.path.dirname(target), exist_ok=True)
            with open(target, "wb") as out:
                out.write(z.read(name))
            print(name)


def cmd_jar_update(jar, src):
    """Rewrites the jar, keeping order, compression and timestamps of all
    entries; only the content of replaced entries changes."""
    tmp = jar + ".tmp"
    replaced = 0
    with zipfile.ZipFile(jar) as old, zipfile.ZipFile(tmp, "w") as new:
        new.comment = old.comment
        for info in old.infolist():
            candidate = os.path.join(src, info.filename)
            if not info.filename.endswith("/") and os.path.isfile(candidate):
                with open(candidate, "rb") as f:
                    new.writestr(info, f.read())
                replaced += 1
            else:
                new.writestr(info, old.read(info))
    os.replace(tmp, jar)
    print("%s: %d entries replaced" % (os.path.basename(jar), replaced), file=sys.stderr)


def main(argv):
    if len(argv) == 3 and argv[1] == "files":
        cmd_files(argv[2])
    elif len(argv) == 4 and argv[1] == "jar-extract":
        cmd_jar_extract(argv[2], argv[3])
    elif len(argv) == 4 and argv[1] == "jar-update":
        cmd_jar_update(argv[2], argv[3])
    else:
        sys.exit(__doc__)


if __name__ == "__main__":
    main(sys.argv)
