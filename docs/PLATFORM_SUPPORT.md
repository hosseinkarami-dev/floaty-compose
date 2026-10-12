# Platform support

Floaty keeps its rendering and state API in Kotlin common code. The Gradle module currently declares Android, JVM, iOS Arm64, iOS Simulator Arm64, Kotlin/JS, and Kotlin/WasmJS targets. `ToastPosition` supports nine placements on every target: `TopStart`, `TopCenter`, `TopEnd`, `CenterStart`, `Center`, `CenterEnd`, `BottomStart`, `BottomCenter`, and `BottomEnd`.

| Platform | Configured target | Notes |
| --- | --- | --- |
| Android | Android KMP target | Stable. CI assembles the demo APK. Insets follow Compose `WindowInsets.safeDrawing`. |
| iOS | `iosArm64`, `iosSimulatorArm64` | Stable. CI compiles both targets on macOS; final Apple binaries require macOS/Xcode. |
| Desktop | JVM | Stable. CI compiles the desktop sample. Insets depend on the window/app host. |
| Browser | JS and WasmJS | Stable. CI compiles both web outputs; behavior depends on the selected Compose/browser runtime. |

## Build state

Floaty is stable on Android, iOS, desktop, and web. GitHub Actions compiles the Android, JVM/desktop, JS, and WasmJS paths on Linux and iOS targets on macOS.

The `v0.1.0` JitPack build succeeded and publishes Android, iOS Arm64, iOS Simulator Arm64, JVM, JS, and WasmJS modules.

Kotlin/Native documentation states Linux can produce Apple `.klib` artifacts only when no cinterop dependencies are involved, and final Apple binaries require macOS. JitPack's normal builds run on Linux, so CI also runs the KMP `publishToMavenLocal` task there. See [Kotlin/Native supported hosts](https://kotlinlang.org/docs/native-target-support.html).

## Safe areas

`ToastHost` applies `Modifier.safeDrawingPadding()` by default. This keeps the toast clear of system bars and cutouts where the host exposes insets. Turn it off when an ancestor already applies safe-area padding. Browser CSS safe-area handling is not implied by this Compose modifier; integrate it in the app's browser host if necessary.

## Compatibility policy

Floaty is stable. Kotlin, Compose Multiplatform, Android Gradle Plugin, browser, and target support versions are tied to the version catalog.
