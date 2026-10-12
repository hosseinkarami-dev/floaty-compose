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

This confirms the configured publications can be generated on that machine. It does not prove that JitPack's Linux build can produce each target or that a consuming application resolves the artifacts correctly. Test a clean consumer project against the exact tagged version before announcing it.

## JitPack workflow

JitPack builds the GitHub commit selected by a tag and exposes artifacts after the build succeeds. The repository includes `jitpack.yml` to select JDK 17. JitPack's Gradle documentation describes multi-module coordinates and its Gradle build flow; see [JitPack build documentation](https://docs.jitpack.io/building/).

Suggested release sequence:

1. Review public API and update `CHANGELOG.md` and compatibility notes.
2. Run common tests and available Android, JVM, JS, and WasmJS compilation checks; build iOS on macOS/Xcode.
3. Run `:floaty:publishToMavenLocal` and inspect generated POM/module metadata.
4. Test Android/JVM consumption from a clean sample project. Test iOS consumption independently on macOS.
5. Commit the release, create and push a version tag (for example, `0.1.0`), then request that version on JitPack.
6. Inspect the JitPack build log and resolve any failed publication before sharing installation instructions as released.
7. Verify the exact dependency in a fresh consumer after JitPack reports success.

## Kotlin Multiplatform and JitPack limits

JitPack's standard builder is Linux-based. Kotlin documents that Linux can produce Apple `.klib` artifacts only when the project has no cinterop dependencies; producing final Apple binaries requires a macOS host. The Compose/Kotlin dependency graph must be checked at release time, so this repository does not claim that JitPack publishes usable iOS variants until the tagged build and a clean iOS consumer prove it. See [Kotlin/Native supported hosts](https://kotlinlang.org/docs/native-target-support.html).

For a release where JitPack can publish Android/JVM but not the iOS variant, state that limitation in the release notes and provide iOS consumers with a macOS-built Maven publication or another repository capable of hosting the complete KMP metadata. Do not silently call an Android-only artifact full multiplatform support.

## Credentials and signing

JitPack's public build does not need repository write credentials for this project. Do not commit signing keys, tokens, or local Gradle credentials. If a separate Maven repository is added, supply credentials through CI secrets and document its consumer setup.

## Current release readiness

- Maven publications are configured for the `:floaty` KMP module.
- Local Maven publication was verified on macOS previously.
- JitPack JDK is configured in `jitpack.yml`.
- A pushed release tag and successful remote JitPack build have not been verified in this worktree.
- The publishing environment has not been verified for iOS artifacts.
