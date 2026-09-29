#!/bin/sh
# Starts VANTED from the installation directory or the unpacked Linux archive.
# The bootstrap derives its paths from the location of vanted-boot.jar.
DIR=$(cd "$(dirname "$0")" && pwd)
cd "$DIR" || exit 1

# Bundled runtime (Linux archive) if present, otherwise the system Java.
if [ -x "$DIR/runtime/bin/java" ] ; then
	JAVA="$DIR/runtime/bin/java"
	echo "using bundled runtime"
else
	JAVA=java
	echo "using system java"
fi

# Heap: a third of physical memory, at most 1 GiB on 32 bit and 8 GiB on 64 bit.
# $HOME/.vanted/startparams replaces all JVM options.
echo "setting startup parameters"

vailmem=$(awk '/MemTotal:/ {print $2}' /proc/meminfo 2>/dev/null)
vailmem=$(( ${vailmem:-0} / 1024 ))
usemem=$(( vailmem / 3 ))

echo "available memory: $vailmem MiB"

isarch=$("$JAVA" -version 2>&1 | grep -c -i "64-bit")
if [ 0 -eq "$isarch" ] ; then
	arch=x86
	echo "32 bit java installed"
else
	arch=x86_64
	echo "64 bit java installed"
fi

if [ "$usemem" -gt 1000 ] ; then
	case $arch in
	'x86')
		usemem=1024
		;;
	*)
		if [ "$usemem" -gt 8000 ] ; then
			usemem=8000
		fi
		;;
	esac
fi

# Fall back to a sane default rather than an invalid -Xmx.
if [ "$usemem" -le 0 ] ; then
	echo "could not determine physical memory, using default"
	usemem=1024
fi

echo "memory for vanted: $usemem MiB"
if [ -e "$HOME/.vanted/startparams" ] ; then
	# shellcheck disable=SC2046  # startparams holds several options
	exec "$JAVA" -Dfile.encoding=UTF-8 $(cat "$HOME/.vanted/startparams") -jar "$DIR/vanted-boot.jar"
else
	exec "$JAVA" -Dfile.encoding=UTF-8 -Xmx${usemem}m -jar "$DIR/vanted-boot.jar"
fi
