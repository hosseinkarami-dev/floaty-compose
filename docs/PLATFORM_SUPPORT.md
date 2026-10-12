# Platform support

Floaty keeps its rendering and state API in Kotlin common code. The Gradle module currently declares Android, JVM, iOS Arm64, iOS Simulator Arm64, Kotlin/JS, and Kotlin/WasmJS targets. `ToastPosition` supports nine placements on every target: `TopStart`, `TopCenter`, `TopEnd`, `CenterStart`, `Center`, `CenterEnd`, `BottomStart`, `BottomCenter`, and `BottomEnd`.

| Platform | Configured target | Notes |
| --- | --- | --- |
| Android | Android KMP target | Host should be placed inside the Compose UI tree. Insets follow Compose `WindowInsets.safeDrawing`. |
| iOS | `iosArm64`, `iosSimulatorArm64` | Requires macOS/Xcode for final Apple binaries and simulator validation. Use the Compose UI host in the shared UI hierarchy. |
| Desktop | JVM | Insets depend on the window/app host; desktop normally has no mobile system bars. |
| Browser | JS and WasmJS | Browser support and safe-area behavior depend on the selected Compose/browser runtime. Test each browser and target you ship. |

## Build state

Target declarations indicate configured compilation paths, not a promise of runtime compatibility across all OS versions, browsers, or toolchains. The repository currently has successful local Android and JVM library compilation; JS, WasmJS, iOS, and consuming-app runtime behavior require their own release checks.

Kotlin/Native documentation states Linux can produce Apple `.klib` artifacts only when no cinterop dependencies are involved, and final Apple binaries require macOS. This is relevant to JitPack because its normal builds run on Linux. Whether the full Compose dependency graph in a given release is suitable for producing all iOS publications there must be verified from the actual tagged build. See [Kotlin/Native supported hosts](https://kotlinlang.org/docs/native-target-support.html).

## Safe areas

`ToastHost` applies `Modifier.safeDrawingPadding()` by default. This keeps the toast clear of system bars and cutouts where the host exposes insets. Turn it off when an ancestor already applies safe-area padding. Browser CSS safe-area handling is not implied by this Compose modifier; integrate it in the app's browser host if necessary.

## Compatibility policy

The project is pre-1.0. Kotlin, Compose Multiplatform, Android Gradle Plugin, browser, and target support versions are tied to the version catalog and may change. Check the Gradle configuration and this document for current status before adopting Floaty for a platform-critical release.
