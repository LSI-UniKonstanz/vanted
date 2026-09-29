# Releasing VANTED

Releases are built and published by GitHub Actions
([`release.yml`](.github/workflows/release.yml)). The GitHub Release is the only
place binaries are published; nothing is built, copied or uploaded by hand.

## Roles

| Role | Can |
|---|---|
| Contributor | open pull requests |
| Maintainer (write access) | review and merge pull requests into `master` |
| Release manager (team `vanted-release`) | create `v*` tags, approve the `release` environment, review changes to the release process |

Every release needs two release managers: one creates the tag, another approves
it. Changes to `.github/`, `packaging/`, `build.xml` and `build.number` need a
release manager's review ([`CODEOWNERS`](.github/CODEOWNERS)).

## Versioning

[Semantic Versioning](https://semver.org), `MAJOR.MINOR.PATCH`. The version is
`vanted.version.number` in `src/main/resources/build.number`; the tag is `v`
followed by that version. A published version is never reused. `build.date` is
set by the build from the date of the tagged commit.

## Making a release

1. Open a pull request that sets `vanted.version.number` and updates
   `CHANGELOG`. It needs an approval and a green `build` workflow.
2. After the merge, a release manager tags the merge commit:
   ```sh
   git switch master && git pull
   git tag -a v2.9.0 -m "VANTED 2.9.0"
   git push origin v2.9.0
   ```
3. A second release manager approves the pending `release` deployment in the
   workflow run.
4. The workflow publishes the release. Its notes link to the workflow run and
   name the commits of `vanted`, `vanted-libraries` and `vanted-bootstrap`.

Assets have no version in their name, so these links always point to the
latest release:

```
https://github.com/LSI-UniKonstanz/vanted/releases/latest/download/vanted-install.exe
https://github.com/LSI-UniKonstanz/vanted/releases/latest/download/vanted-macos-arm64.dmg
https://github.com/LSI-UniKonstanz/vanted/releases/latest/download/vanted-linux-x64.tar.gz
https://github.com/LSI-UniKonstanz/vanted/releases/latest/download/vanted-linux-arm64.tar.gz
https://github.com/LSI-UniKonstanz/vanted/releases/latest/download/vanted-install.jar
```

The built-in updater of VANTED and the old download server are not updated by
this process.

## Checks

| When | What |
|---|---|
| Every pull request | `build`, `package-installer`, `package-linux`, `package-macos`: compile, plugin list, installers, Linux archives, macOS DMG |
| Before the approval | tag is annotated, on `master`, matches `build.number`, not yet released |
| Before publishing | the EXE is readable after signing; the macOS runtime starts under the hardened runtime; notarization and Gatekeeper assessment (once signing is configured) |

The unit tests in `src/test` are not run by CI.

The two other repositories are pinned to commits in
[`build.yml`](.github/workflows/build.yml); a new commit there reaches a
release only through a pull request that updates the pin.

## When a release fails

Nothing is published until every job has succeeded and all assets are
uploaded.

- **Transient failure** (runner, network, SignPath timeout): re-run the failed
  jobs. A draft left by an aborted run is replaced.
- **The code needs a fix** and nothing was published: merge the fix, then a
  release manager deletes the tag and tags the fixed commit.
- **Something was published**: treat it as a broken release.

## Broken release

Published files are never replaced.

1. Mark the release as a pre-release and put a warning at the top of its notes.
   The `latest` download links then point to the previous release.
   ```sh
   gh release edit v2.9.0 --prerelease
   ```
2. Publish the fix as the next patch version.

Delete a release only if it must not be downloaded at all (for example because
it contains credentials); keep the tag, and rotate any exposed credentials.

## Hotfix

A hotfix is a patch release from `master` and takes the normal path, including
the review. There are no maintenance branches and no way to bypass the rules;
ask a second release manager to review and approve promptly.

## Updating pinned versions

Nothing updates automatically. Update in a pull request, and test the
installers on Windows and macOS when launch4j or Corretto change.

| What | Where |
|---|---|
| GitHub Actions | `.github/workflows/*.yml`: commit SHA with the version as comment; `gh api repos/<owner>/<action>/commits/<tag> --jq .sha` gives the SHA |
| IzPack, launch4j | `packaging/pom.xml` (IzPack 5.2 and later need Java 9+ to build) |
| Corretto | `packaging/fetch-tools.sh`, version and SHA-256 values |
| `vanted-libraries`, `vanted-bootstrap` | `VANTED_*_REF` in `.github/workflows/build.yml` |

GitHub's Dependabot alerts report known vulnerabilities in these dependencies.

## Repository setup

One-time settings in `LSI-UniKonstanz/vanted`:

- **Team** `vanted-release` with write access, at least two members.
- **Ruleset for `master`**: require a pull request with 1 approval, dismiss
  stale approvals, require review from code owners, require the status checks
  `build`, `package-installer`, `package-linux` and `package-macos`, block
  force pushes and deletion. No bypass.
- **Ruleset for tags `v*`**: restrict creation, update and deletion. Bypass:
  `vanted-release`.
- **Environment `release`**: required reviewers `vanted-release`, prevent
  self-review, deployment tags `v*`. Signing credentials are stored here, see
  [`packaging/SIGNING.md`](packaging/SIGNING.md).
- **Actions**: default workflow permissions read-only; Actions may not approve
  pull requests.
- **Security**: secret scanning with push protection, Dependabot alerts.
- **Releases**: enable immutable releases if the repository offers it.
