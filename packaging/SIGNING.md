# Signing

The signing jobs in [`release.yml`](../.github/workflows/release.yml) run after
the `release` environment is approved; only then can they read its secrets.

| Job | With credentials | Without credentials |
|---|---|---|
| `sign-windows` | signs `vanted-install.exe` via SignPath | passes the EXE through unsigned, with a warning |
| `sign-macos` | signs the app and DMG with the Developer ID, notarizes | signs the app ad hoc as a dry run (same script, same checks) and publishes the unsigned DMG |

Partially configured credentials fail the release instead of silently
publishing unsigned files. Once signing works, set `WINDOWS_SIGNING_REQUIRED`
and `MACOS_SIGNING_REQUIRED` to `true` so that missing credentials fail the
release as well.

## Credentials

All of these are stored in the environment **`release`**, not as repository
secrets: environment secrets are only readable by the jobs that use the
environment, after approval.

| Name | Kind | Content |
|---|---|---|
| `SIGNPATH_API_TOKEN` | secret | API token of the SignPath CI user (role *Submitter*) |
| `SIGNPATH_ORGANIZATION_ID` | variable | SignPath organization ID |
| `SIGNPATH_PROJECT_SLUG` | variable | optional, default `vanted` |
| `SIGNPATH_SIGNING_POLICY_SLUG` | variable | optional, default `release-signing` |
| `SIGNPATH_ARTIFACT_CONFIGURATION_SLUG` | variable | optional, default configuration otherwise |
| `WINDOWS_SIGNING_REQUIRED` | variable | `true`: fail without SignPath |
| `MACOS_CERTIFICATE_P12` | secret | *Developer ID Application* certificate with private key, `.p12`, base64 |
| `MACOS_CERTIFICATE_PASSWORD` | secret | password of the `.p12` |
| `APPLE_NOTARY_KEY` | secret | content of `AuthKey_XXXXXXXXXX.p8` (App Store Connect API) |
| `APPLE_NOTARY_KEY_ID` | variable | key ID |
| `APPLE_NOTARY_ISSUER_ID` | variable | issuer ID |
| `MACOS_SIGNING_REQUIRED` | variable | `true`: fail without Developer ID |

## Windows: SignPath Foundation

The key stays in SignPath's HSM; GitHub only holds an API token. SignPath
verifies through its GitHub app that the file comes from this workflow run on
a GitHub-hosted runner, and every request is approved manually at SignPath.

1. Apply to the SignPath Foundation; it creates the project and provides the
   organization ID and slugs.
2. Install the SignPath GitHub app on the repository.
3. Enter [`windows/signpath-artifact-configuration.xml`](windows/signpath-artifact-configuration.xml)
   as the project's artifact configuration.
4. Allow the release tags (`v*`) in the signing policy.
5. Create a CI user, store its token and the organization ID (table above).
6. Publish the code signing policy the Foundation requires on vanted.org:
   the attribution *"Free code signing provided by SignPath.io, certificate by
   SignPath Foundation"*, the committers, reviewers and approvers by name, and
   a privacy statement.
7. Run a test release, then set `WINDOWS_SIGNING_REQUIRED` to `true`.

SignPath evaluates at most three re-runs of a workflow run; after that, a new
tag is needed.

launch4j must start the installer in classpath mode (`<classPath>` in
[`launch4j/vanted-l4j.xml`](launch4j/vanted-l4j.xml)); in `java -jar` mode the
signed EXE fails with "Invalid or corrupt jarfile". `build.yml` checks the
configuration and `windows/CheckExeJar.java` loads the jar from the signed EXE.

## macOS: Developer ID and notarization

`sign-macos` unpacks the app from the unsigned DMG, imports the certificate into
a temporary keychain, runs [`macos/sign-app.sh`](macos/sign-app.sh), starts the
bundled JVM and loads the Adaptagrams library under the hardened runtime, then
builds, signs and notarizes the DMG and checks it with `spctl`.

1. Create a *Developer ID Application* certificate in the university's Apple
   Developer account (account holder or admin).
2. Export certificate and private key as `.p12` with a password;
   `base64 -i developer-id.p12 | pbcopy` gives `MACOS_CERTIFICATE_P12`. Delete
   the `.p12` afterwards.
3. Create an App Store Connect API key (*Users and Access → Integrations →
   Team Keys*, role *Developer*). The `.p8` can be downloaded only once.
4. Run a test release, then set `MACOS_SIGNING_REQUIRED` to `true`.

To try signing locally, on a copy of the app from a CI DMG:

```sh
hdiutil attach vanted-2.9.0.dmg
ditto /Volumes/VANTED/Vanted.app /tmp/Vanted.app
hdiutil detach /Volumes/VANTED
MACOS_SIGN_IDENTITY=- sh packaging/macos/sign-app.sh /tmp/Vanted.app         # ad hoc
MACOS_SIGN_IDENTITY=<SHA-1> sh packaging/macos/sign-app.sh /tmp/Vanted.app   # Developer ID
```

`security find-identity -v -p codesigning` lists the SHA-1 of installed identities.

Troubleshooting:

- **Notarization rejected**: `notarize.sh` prints Apple's log, which names each
  rejected file. If it is inside a jar, `macos/macho.py` did not detect it.
- **"unable to build chain"**: intermediate certificate missing; the workflow
  imports `DeveloperIDG2CA.cer` from Apple.
- **App crashes at start**: missing entitlement; the smoke test should catch it.

The notarization ticket is stapled to the DMG. For the app copied out of it,
Gatekeeper checks online on first launch. Only arm64 is built.

## First signed release

- Windows: run the EXE on a Windows machine; *Properties → Digital Signatures*
  shows the publisher.
- macOS: download the DMG in a browser, drag the app to Applications and start
  it without the right-click workaround; Activity Monitor must show *Apple* as
  kind. Run the edge routing layout once (loads the signed Adaptagrams library).
- Then set both `*_SIGNING_REQUIRED` variables to `true`.
