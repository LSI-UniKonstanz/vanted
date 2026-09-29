#!/bin/sh
# Builds the VANTED DMG on macOS.
#
#   ./createdmg.sh <path/to/Vanted.app> <target.dmg> [volume name]
set -eu

APP=${1:?path to Vanted.app missing}
OUT=${2:?target DMG missing}
VOLNAME=${3:-VANTED}

SRCDIR=$(cd "$(dirname "$0")"; pwd)
STAGE=$(mktemp -d)
trap 'rm -rf "$STAGE"' EXIT

cp -a "$APP" "$STAGE/"
ln -s /Applications "$STAGE/Applications"

# Window background and icon positions (.DS_Store)
[ -d "$SRCDIR/.background" ]     && cp -a "$SRCDIR/.background"     "$STAGE/"
[ -f "$SRCDIR/.DS_Store" ]       && cp -a "$SRCDIR/.DS_Store"       "$STAGE/"
if [ -f "$SRCDIR/.VolumeIcon.icns" ] ; then
	cp -a "$SRCDIR/.VolumeIcon.icns" "$STAGE/"
	SetFile -a C "$STAGE" 2>/dev/null || true
fi

rm -f "$OUT"
# hdiutil occasionally fails with "Resource busy" on hosted runners.
for try in 1 2 3; do
	if hdiutil create -volname "$VOLNAME" -srcfolder "$STAGE" -ov -format UDZO "$OUT"; then
		echo "DMG: $OUT"
		exit 0
	fi
	echo "hdiutil create failed (attempt $try)" >&2
	sleep 10
done
exit 1
