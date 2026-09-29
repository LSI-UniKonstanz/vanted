#!/bin/sh
# Signs Vanted.app for notarization, inside out.
#
#   MACOS_SIGN_IDENTITY=<SHA-1 of the Developer ID> ./sign-app.sh <path/to/Vanted.app>
#   MACOS_SIGN_IDENTITY=-                           ./sign-app.sh ...   (ad hoc)
#
# Ad hoc ("-") needs no certificate; CI uses it as a dry run until a Developer ID
# is available.
#
# Signs the bundle in place: run it on a copy, never on packaging/macos/Vanted.app.
#
# Order matters, because signing a bundle seals the hashes of its contents:
#   1. native libraries inside jars (extract, sign, put back)
#   2. loose binaries (JVM, dylibs; executables get the entitlements)
#   3. frameworks
#   4. the runtime bundle (Contents/Java/runtime)
#   5. the app
set -eu

APP=${1:?path to Vanted.app missing}
ID=${MACOS_SIGN_IDENTITY:?MACOS_SIGN_IDENTITY missing: SHA-1 of the Developer ID, or - for ad hoc}
HERE=$(cd "$(dirname "$0")" && pwd)
ENTITLEMENTS="$HERE/entitlements.plist"
APP=$(cd "$APP" && pwd)

[ -f "$APP/Contents/Info.plist" ] || { echo "not an app bundle: $APP" >&2; exit 1; }

# A timestamp needs a real identity and is required for notarization.
if [ "$ID" = "-" ]; then
	TIMESTAMP=--timestamp=none
	echo ">> signing ad hoc (dry run, cannot be notarized)"
else
	TIMESTAMP=--timestamp
	echo ">> signing with $ID"
fi

sign() {
	codesign --force "$TIMESTAMP" --options runtime --sign "$ID" "$@"
}

WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT

# codesign rejects extended attributes ("resource fork, Finder information, or
# similar detritus not allowed").
xattr -cr "$APP"

echo ">> 1/5 native libraries in jars"
find "$APP/Contents" -type f -name '*.jar' | sort > "$WORK/jars"
n=0
while IFS= read -r jar; do
	out="$WORK/jar-$n"; n=$((n + 1))
	mkdir -p "$out"
	python3 "$HERE/macho.py" jar-extract "$jar" "$out" > "$out.list"
	[ -s "$out.list" ] || continue
	while IFS= read -r entry; do
		echo "   ${jar#"$APP"/}!/$entry"
		sign "$out/$entry"
	done < "$out.list"
	python3 "$HERE/macho.py" jar-update "$jar" "$out"
done < "$WORK/jars"

# Entitlements only take effect on executables (MH_EXECUTE). Binaries inside
# frameworks are signed in step 3.
echo ">> 2/5 loose binaries"
python3 "$HERE/macho.py" files "$APP/Contents" > "$WORK/machos"
exe=0; lib=0
while read -r kind path; do
	case $path in *.framework/*) continue ;; esac
	if [ "$kind" = exe ]; then
		sign --entitlements "$ENTITLEMENTS" "$path"
		exe=$((exe + 1))
	else
		sign "$path"
		lib=$((lib + 1))
	fi
done < "$WORK/machos"
echo "   $exe executables, $lib libraries"

echo ">> 3/5 frameworks"
# sort -r: nested frameworks before the outer one
find "$APP/Contents" -type d -name '*.framework' | sort -r > "$WORK/frameworks"
while IFS= read -r fw; do
	echo "   ${fw#"$APP"/}"
	sign "$fw"
done < "$WORK/frameworks"

# Corretto ships as a .jdk bundle; Amazon's seal is broken by step 2 and
# replaced here.
echo ">> 4/5 runtime bundle"
if [ -f "$APP/Contents/Java/runtime/Contents/Info.plist" ]; then
	sign "$APP/Contents/Java/runtime"
else
	echo "   none (runtime without bundle structure)"
fi

# The main executable is starter.sh; the signature goes to Contents/_CodeSignature/.
echo ">> 5/5 app"
sign --entitlements "$ENTITLEMENTS" "$APP"

echo ">> verify"
codesign --verify --deep --strict --verbose=2 "$APP"
codesign --display --verbose=2 "$APP" 2>&1 \
	| grep -E '^(Identifier|Format|Authority|TeamIdentifier|Timestamp|Runtime Version|CodeDirectory)' || true
echo ">> done: $APP"
