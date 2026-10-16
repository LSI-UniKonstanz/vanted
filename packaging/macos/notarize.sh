#!/bin/sh
# Notarizes a file with Apple and staples the ticket.
#
# Three ways to authenticate, checked in this order:
#
#   App Store Connect API key (team key, not tied to a person):
#     NOTARY_KEY_FILE=AuthKey_ABC123.p8 NOTARY_KEY_ID=ABC123 NOTARY_ISSUER_ID=<uuid> \
#     ./notarize.sh vanted-macos-arm64.dmg
#
#   Apple ID of a team member with an app-specific password:
#     NOTARY_APPLE_ID=name@uni-konstanz.de NOTARY_PASSWORD=abcd-efgh-ijkl-mnop \
#     NOTARY_TEAM_ID=ABCDE12345 ./notarize.sh vanted-macos-arm64.dmg
#
#   Profile stored in the login keychain (local Mac only), created once with
#   'xcrun notarytool store-credentials vanted':
#     NOTARY_KEYCHAIN_PROFILE=vanted ./notarize.sh vanted-macos-arm64.dmg
#
# The notary service checks everything inside the DMG, including the jars.
set -eu

FILE=${1:?file missing}

if [ -n "${NOTARY_KEY_FILE:-}" ]; then
	: "${NOTARY_KEY_ID:?NOTARY_KEY_ID missing}"
	: "${NOTARY_ISSUER_ID:?NOTARY_ISSUER_ID missing}"
	echo ">> auth: App Store Connect API key $NOTARY_KEY_ID"
	notary() {
		xcrun notarytool "$@" \
			--key "$NOTARY_KEY_FILE" --key-id "$NOTARY_KEY_ID" --issuer "$NOTARY_ISSUER_ID"
	}
elif [ -n "${NOTARY_APPLE_ID:-}" ]; then
	: "${NOTARY_PASSWORD:?NOTARY_PASSWORD missing (app-specific password)}"
	: "${NOTARY_TEAM_ID:?NOTARY_TEAM_ID missing}"
	echo ">> auth: Apple ID, team $NOTARY_TEAM_ID"
	notary() {
		xcrun notarytool "$@" \
			--apple-id "$NOTARY_APPLE_ID" --password "$NOTARY_PASSWORD" --team-id "$NOTARY_TEAM_ID"
	}
elif [ -n "${NOTARY_KEYCHAIN_PROFILE:-}" ]; then
	echo ">> auth: keychain profile $NOTARY_KEYCHAIN_PROFILE"
	notary() {
		xcrun notarytool "$@" --keychain-profile "$NOTARY_KEYCHAIN_PROFILE"
	}
else
	echo "no notary credentials: set NOTARY_KEY_FILE, NOTARY_APPLE_ID or NOTARY_KEYCHAIN_PROFILE" >&2
	exit 1
fi

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
