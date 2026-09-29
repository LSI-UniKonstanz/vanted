#!/bin/sh
# Downloads the Java runtimes bundled with VANTED. IzPack and launch4j are
# resolved by Maven (packaging/pom.xml).
#
#   ./fetch-tools.sh [dest] [platform ...]      default: .tools macos
#
#   macos          -> <dest>/corretto/Contents/Home              full .jdk bundle
#   windows-x64    -> <dest>/runtime-windows-x64/bin/java.exe    JRE
#   linux-x64      -> <dest>/runtime-linux-x64/bin/java          jre/ from the JDK
#   linux-aarch64  -> <dest>/runtime-linux-aarch64/bin/java      jre/ from the JDK
#
# Version and checksums are pinned here rather than taken from the download
# server. To update, copy the version and all four SHA-256 values from
# https://github.com/corretto/corretto-8/releases and test each platform.
set -eu

CORRETTO_VERSION=8.504.01.1
SHA_MACOS=425eaab72289ffb346fae3d904de7301dc9fcb70ae9dc095fac2ea1b1c621bea
SHA_WINDOWS_X64=53f073e2bb30e1edaa9df4865e909c2aba9bae36f8367a3a7e1cedd6c16f9281
SHA_LINUX_X64=56f0f6ab9b50f69bdf72705542342c0ff9c4e0f531f03d6751dcebe164308983
SHA_LINUX_AARCH64=54d872a37ee35eafdf057ceb2dad2d25c250580db91e7a08f656c945d489a375

DEST=${1:-.tools}
if [ $# -gt 0 ]; then shift; fi
[ $# -gt 0 ] || set -- macos
mkdir -p "$DEST"
DEST=$(cd "$DEST" && pwd)

# fetch <file> <sha256>
fetch() {
	curl -fsSL -o "$DEST/$1" "https://corretto.aws/downloads/resources/$CORRETTO_VERSION/$1"
	( cd "$DEST" && echo "$2  $1" | shasum -a 256 -c - )
}

# jre_from <archive> <dest>: move the directory that contains lib/rt.jar
# (top level of a JRE archive, jre/ of a JDK archive) to <dest>.
jre_from() {
	unpack="$DEST/.unpack"
	rm -rf "$unpack"
	mkdir -p "$unpack"
	case $1 in
		*.zip) unzip -q "$DEST/$1" -d "$unpack" ;;
		*)     tar -xzf "$DEST/$1" -C "$unpack" ;;
	esac
	rtjar=$(find "$unpack" -path '*/lib/rt.jar' | head -n 1)
	[ -n "$rtjar" ] || { echo "no runtime (lib/rt.jar) in $1" >&2; exit 1; }
	rm -rf "$2"
	mv "$(dirname "$(dirname "$rtjar")")" "$2"
	rm -rf "$unpack"
}

for platform in "$@"; do
	case $platform in
	macos)
		tgz=amazon-corretto-$CORRETTO_VERSION-macosx-aarch64.tar.gz
		echo ">> Amazon Corretto $CORRETTO_VERSION (macOS aarch64)"
		fetch "$tgz" "$SHA_MACOS"
		rm -rf "$DEST/corretto"
		mkdir -p "$DEST/corretto"
		tar -xzf "$DEST/$tgz" -C "$DEST/corretto" --strip-components=1
		test -x "$DEST/corretto/Contents/Home/bin/java" || { echo "unpacking failed" >&2; exit 1; }
		echo ">> done: $DEST/corretto"
		;;
	windows-x64)
		zip=amazon-corretto-$CORRETTO_VERSION-windows-x64-jre.zip
		echo ">> Amazon Corretto $CORRETTO_VERSION (Windows x64, JRE)"
		fetch "$zip" "$SHA_WINDOWS_X64"
		jre_from "$zip" "$DEST/runtime-windows-x64"
		test -f "$DEST/runtime-windows-x64/bin/java.exe" || { echo "java.exe missing" >&2; exit 1; }
		echo ">> done: $DEST/runtime-windows-x64"
		;;
	linux-x64|linux-aarch64)
		arch=${platform#linux-}
		tgz=amazon-corretto-$CORRETTO_VERSION-linux-$arch.tar.gz
		if [ "$arch" = x64 ]; then sha=$SHA_LINUX_X64; else sha=$SHA_LINUX_AARCH64; fi
		echo ">> Amazon Corretto $CORRETTO_VERSION (Linux $arch, JRE from the JDK)"
		fetch "$tgz" "$sha"
		jre_from "$tgz" "$DEST/runtime-$platform"
		test -x "$DEST/runtime-$platform/bin/java" || { echo "bin/java missing" >&2; exit 1; }
		echo ">> done: $DEST/runtime-$platform"
		;;
	*)
		echo "unknown platform: $platform (macos, windows-x64, linux-x64, linux-aarch64)" >&2
		exit 2
		;;
	esac
done
