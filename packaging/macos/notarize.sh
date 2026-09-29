#!/bin/sh
# Notarizes a file with Apple and staples the ticket.
#
#   NOTARY_KEY_FILE=AuthKey_ABC123.p8 \
#   NOTARY_KEY_ID=ABC123 \
#   NOTARY_ISSUER_ID=<uuid> \
#   ./notarize.sh vanted-macos-arm64.dmg
#
# Uses an App Store Connect API key, which is not tied to a person's Apple ID.
# The notary service checks everything inside the DMG, including the jars.
set -eu

FILE=${1:?file missing}
: "${NOTARY_KEY_FILE:?NOTARY_KEY_FILE missing (path to the .p8 file)}"
: "${NOTARY_KEY_ID:?NOTARY_KEY_ID missing}"
: "${NOTARY_ISSUER_ID:?NOTARY_ISSUER_ID missing}"

notary() {
	xcrun notarytool "$@" \
		--key "$NOTARY_KEY_FILE" --key-id "$NOTARY_KEY_ID" --issuer "$NOTARY_ISSUER_ID"
}

field() { # field <json file> <name>
	python3 -c 'import json, sys; print(json.load(open(sys.argv[1])).get(sys.argv[2], ""))' "$1" "$2" 2>/dev/null || true
}

RESULT=$(mktemp)
trap 'rm -f "$RESULT"' EXIT

echo ">> submitting: $FILE"
# Do not abort here: on "Invalid" the log below says why.
notary submit "$FILE" --wait --timeout 2h --output-format json > "$RESULT" || true
cat "$RESULT"; echo

status=$(field "$RESULT" status)
id=$(field "$RESULT" id)

if [ "$status" != "Accepted" ]; then
	echo "notarization failed (status: ${status:-unknown})" >&2
	# The log lists every rejected file, usually a binary without signature,
	# timestamp or hardened runtime.
	[ -n "$id" ] && notary log "$id"
	exit 1
fi

echo ">> stapling ticket"
xcrun stapler staple "$FILE"
xcrun stapler validate "$FILE"
echo ">> notarized: $FILE"
