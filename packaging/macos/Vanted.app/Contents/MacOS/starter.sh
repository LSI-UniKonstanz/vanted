#!/bin/sh
# VANTED launcher and CFBundleExecutable of the bundle. With no native binary
# in the bundle, the app runs on the architecture of the JVM it starts.

DIR=$(cd "$(dirname "$0")"; cd ..; pwd)

# Bundled runtime; system Java for development builds without one.
if [ -x "$DIR/Java/runtime/Contents/Home/bin/java" ] ; then
	JAVA="$DIR/Java/runtime/Contents/Home/bin/java"
elif [ -x "$DIR/Java/runtime/bin/java" ] ; then
	JAVA="$DIR/Java/runtime/bin/java"
elif [ -x /usr/libexec/java_home ] ; then
	JAVA="$(/usr/libexec/java_home -v 1.8 2>/dev/null)/bin/java"
	[ -x "$JAVA" ] || JAVA="$(/usr/libexec/java_home 2>/dev/null)/bin/java"
else
	JAVA=java
fi

if [ ! -x "$JAVA" ] ; then
	osascript -e 'display alert "VANTED" message "No Java runtime found."' 2>/dev/null
	exit 1
fi

# Heap in GB: the larger of half the free memory and a third of physical
# memory, at least 1. $HOME/.vanted/startparams overrides it.
FREE_BLOCKS=$(vm_stat | awk '/Pages free/            {gsub(/\./,"",$3); print $3}')
SPEC_BLOCKS=$(vm_stat | awk '/Pages speculative/     {gsub(/\./,"",$3); print $3}')
FREEMEM=$(( (FREE_BLOCKS + SPEC_BLOCKS) * 4096 / 1024 / 1024 / 1024 ))
TOTALMEM=$(( $(sysctl -n hw.memsize) / 1024 / 1024 / 1024 ))

usemem=$(( FREEMEM / 2 > TOTALMEM / 3 ? FREEMEM / 2 : TOTALMEM / 3 ))
[ "$usemem" -lt 1 ] && usemem=1

if [ -e "$HOME/.vanted/startparams" ] ; then
	usemem=$(cat "$HOME/.vanted/startparams")
fi

exec "$JAVA" \
	-Xmx"${usemem}"g \
	-Xdock:name=VANTED \
	-Xdock:icon="$DIR/Resources/vantedicon.icns" \
	-Dapple.laf.useScreenMenuBar=true \
	-Dcom.apple.macos.use-file-dialog-packages=true \
	-Dcom.apple.mrj.application.apple.menu.about.name=VANTED \
	-jar "$DIR/Java/vanted-boot.jar" "$@"
