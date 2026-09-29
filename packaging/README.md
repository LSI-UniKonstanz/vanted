# packaging/

Configuration for the installers and bundles. CI runs all of this through
[`build.yml`](../.github/workflows/build.yml); the release process is described
in [`RELEASING.md`](../RELEASING.md), signing in [`SIGNING.md`](SIGNING.md).

| Release asset | For | Java |
|---|---|---|
| `vanted-install.exe` | Windows | bundles Corretto 8; the installer itself needs Java 8–17 |
| `vanted-macos-arm64.dmg` | Macs with Apple silicon | bundles Corretto 8 |
| `vanted-linux-x64.tar.gz`, `vanted-linux-arm64.tar.gz` | Linux | bundle Corretto 8, no installer |
| `vanted-install.jar` | everything else (e.g. Intel Macs) | needs an installed Java 8 |

| Path | Content |
|---|---|
| `pom.xml` | builds the installer jar and, with profile `windows-runtime`, the Windows EXE |
| `izpack/` | IzPack installer definition, language files, launcher scripts |
| `launch4j/` | Windows launcher configuration, icon, splash |
| `macos/` | `Vanted.app` skeleton, DMG layout, `createdmg.sh`, signing (`sign-app.sh`, `notarize.sh`, `macho.py`, `entitlements.plist`, `NativeLoadCheck.java`) |
| `windows/` | SignPath artifact configuration, `CheckExeJar.java` |
| `fetch-tools.sh` | downloads Amazon Corretto 8 with pinned version and SHA-256 |

## Building locally

Requires the assembled `dist/` (`vanted-boot.jar`, `vanted-core/`,
`core-libs/`), as produced by the `build` job.

```sh
# Installer for all platforms, without runtime
mvn -f packaging/pom.xml package \
    -Dvanted.version=2.9.0 -Dvanted.build=1 -Dvanted.package.dir=$PWD/dist

# Windows installer with runtime
packaging/fetch-tools.sh .tools windows-x64
mvn -f packaging/pom.xml package \
    -Dvanted.version=2.9.0 -Dvanted.build=1 -Dvanted.package.dir=$PWD/dist \
    -Dvanted.runtime.windows=$PWD/.tools/runtime-windows-x64

# macOS DMG (on a Mac)
packaging/fetch-tools.sh .tools macos
cp -a .tools/corretto packaging/macos/Vanted.app/Contents/Java/runtime
cp -a dist/* packaging/macos/Vanted.app/Contents/Java/
packaging/macos/createdmg.sh packaging/macos/Vanted.app vanted-2.9.0.dmg
```

Results are in `packaging/target/`. The second Maven run uses
`target/staging-windows/` so that the two installers do not interfere.

On Apple silicon the launch4j step needs Rosetta 2, because launch4j ships
x86_64 binaries.

## Placeholders

`izpack/config.xml` and `launch4j/vanted-l4j.xml` contain `@{...}`, which Maven
substitutes when staging. IzPack's own `${...}` variables are left alone.

| Placeholder | Source |
|---|---|
| `@{vanted.version}`, `@{vanted.build}` | command line |
| `@{product.name}`, `@{app.website}` | `pom.xml` |
| `@{installer.jar}`, `@{installer.exe}`, `@{icon.file}`, `@{splash.file}` | `pom.xml` |
| `@{windows.runtime.fileset}` | profile `windows-runtime`, empty otherwise |

`macos/Vanted.app/Contents/Info.plist` uses `@VANTED_VERSION@` and
`@VANTED_BUILD@`, which the `package-macos` job fills in.
