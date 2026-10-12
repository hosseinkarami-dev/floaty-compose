# Platform support

Floaty keeps its rendering and state API in Kotlin common code. The Gradle module currently declares Android, JVM, iOS Arm64, iOS Simulator Arm64, Kotlin/JS, and Kotlin/WasmJS targets. `ToastPosition` supports nine placements on every target: `TopStart`, `TopCenter`, `TopEnd`, `CenterStart`, `Center`, `CenterEnd`, `BottomStart`, `BottomCenter`, and `BottomEnd`.

| Platform | Configured target | Notes |
| --- | --- | --- |
| Android | Android KMP target | Runtime checked by the maintainer. CI assembles the demo APK. Insets follow Compose `WindowInsets.safeDrawing`. |
| iOS | `iosArm64`, `iosSimulatorArm64` | Runtime checked by the maintainer. CI compiles both targets on macOS; final Apple binaries require macOS/Xcode. |
| Desktop | JVM | Runtime checked by the maintainer. CI compiles the desktop sample. Insets depend on the window/app host. |
| Browser | JS and WasmJS | Runtime checked by the maintainer. CI compiles both web outputs; behavior depends on the selected Compose/browser runtime. |

## Build state

The maintainer has manually checked the app at runtime on Android, iOS, desktop, and web and reports that it works correctly. GitHub Actions is configured to compile the Android, JVM/desktop, JS, and WasmJS paths on Linux and compile iOS targets on macOS. These checks do not cover every OS version, browser, device, or accessibility configuration.

Kotlin/Native documentation states Linux can produce Apple `.klib` artifacts only when no cinterop dependencies are involved, and final Apple binaries require macOS. This is relevant to JitPack because its normal builds run on Linux. CI is configured to run the KMP `publishToMavenLocal` task on Linux to catch publication/build incompatibilities; the exact tagged JitPack artifact and a clean iOS consumer still need verification for each first release. See [Kotlin/Native supported hosts](https://kotlinlang.org/docs/native-target-support.html).

## Safe areas

`ToastHost` applies `Modifier.safeDrawingPadding()` by default. This keeps the toast clear of system bars and cutouts where the host exposes insets. Turn it off when an ancestor already applies safe-area padding. Browser CSS safe-area handling is not implied by this Compose modifier; integrate it in the app's browser host if necessary.

## Compatibility policy

The project is pre-1.0. Kotlin, Compose Multiplatform, Android Gradle Plugin, browser, and target support versions are tied to the version catalog and may change. Check the Gradle configuration and this document for current status before adopting Floaty for a platform-critical release.
