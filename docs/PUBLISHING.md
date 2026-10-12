# JitPack publishing and releases

## Coordinates

The Git remote for this checkout is `hosseinkarami-dev/floaty-compose`; the reusable Gradle module is `floaty`. JitPack's multi-module coordinate is therefore:

```text
com.github.hosseinkarami-dev.floaty-compose:floaty:<tag>
```

The root Gradle build sets that group by default. Override it with `-PjitpackGroup=...` if the GitHub owner/repository changes. Set the release version through `-PVERSION_NAME=...` when building locally; JitPack's tag is the consumer-facing dependency version.

## Local artifact check

From a clean checkout on macOS, build the Maven publications locally:

```sh
./gradlew :floaty:publishToMavenLocal
```

This confirms the configured publications can be generated on that machine. CI also runs the same library publication on Linux, the host used by JitPack, and tests a clean consumer against the exact tagged version remain a release check.

## JitPack workflow

JitPack builds the GitHub commit selected by a tag and exposes artifacts after the build succeeds. The repository's `jitpack.yml` selects JDK 21 and runs only `:floaty:publishToMavenLocal`, passing JitPack's group and version into Gradle. JitPack's Gradle documentation describes multi-module coordinates and the custom install command; see [JitPack build documentation](https://docs.jitpack.io/building/).

## Automated workflows

`.github/workflows/ci.yml` runs on pull requests and pushes to `main`. Its Linux job runs library JVM tests, compiles Android/JVM/JS/WasmJS, checks Maven-local publication, assembles the Android sample, and compiles desktop and browser samples. Its macOS job compiles the library and shared sample for iOS device and simulator targets.

`.github/workflows/release.yml` runs when a `v*` tag is pushed. It first calls the same CI workflow; after CI succeeds, it verifies local publication for the tag version and requests the JitPack POM URL with retries. A successful request confirms JitPack returned the expected module and version. JitPack builds the artifact from the tag on demand; no Maven credentials or publish token are stored in GitHub Actions. The workflow uses read-only repository permissions.

Suggested release sequence:

1. Review public API and update `CHANGELOG.md` and compatibility notes.
2. Run common tests and available Android, JVM, JS, and WasmJS compilation checks; build iOS on macOS/Xcode.
3. Run `:floaty:publishToMavenLocal` and inspect generated POM/module metadata.
4. Test Android/JVM consumption from a clean sample project. Test iOS consumption independently on macOS.
5. Commit the release and push a version tag (for example, `v0.1.0`).
6. Wait for the Release to JitPack workflow and inspect its run and the [JitPack build log](https://jitpack.io/com/github/hosseinkarami-dev/floaty-compose/v0.1.0/build.log) if it fails.
7. Verify the exact dependency in a fresh Android and iOS consumer after JitPack reports success.

## Kotlin Multiplatform and JitPack limits

JitPack's standard builder is Linux-based. Kotlin documents that Linux can produce Apple `.klib` artifacts only when the project has no cinterop dependencies; producing final Apple binaries requires a macOS host. The Linux CI job runs `:floaty:publishToMavenLocal` to catch incompatibilities before release. A successful CI run is not a substitute for resolving the exact tagged artifact in a clean iOS consumer. See [Kotlin/Native supported hosts](https://kotlinlang.org/docs/native-target-support.html).

For a release where JitPack can publish Android/JVM but not the iOS variant, state that limitation in the release notes and provide iOS consumers with a macOS-built Maven publication or another repository capable of hosting the complete KMP metadata. Do not silently call an Android-only artifact full multiplatform support.

## Credentials and signing

JitPack's public build does not need repository write credentials for this project. Do not commit signing keys, tokens, or local Gradle credentials. If a separate Maven repository is added, supply credentials through CI secrets and document its consumer setup.

## Current release readiness

- Maven publications are configured for the `:floaty` KMP module.
- Local Maven publication was verified on macOS; Linux publication is checked by CI.
- The maintainer reports successful runtime checks on Android, desktop, iOS, and web.
- `v0.1.0` is published successfully on JitPack; the build reports `floaty`, `floaty-android`, `floaty-iosarm64`, `floaty-iossimulatorarm64`, `floaty-js`, `floaty-jvm`, and `floaty-wasm-js` modules.
- The release workflow fetched and checked the `v0.1.0` POM. A clean JVM consumer also resolved the JitPack dependency and compiled against `ToastPosition`; clean Android and iOS consumer resolution is still a useful additional check.
