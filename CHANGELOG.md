# Changelog

## [0.1.8] - 2026-10-12

- Create a GitHub Release after JitPack verification and attach a ZIP containing the Maven publications.

## [0.1.7] - 2026-10-12

- Add Google's Maven Central mirror to avoid JitPack's upstream Maven Central rate limit.

## [0.1.6] - 2026-10-12

- Check Google's Maven repository before Maven Central when resolving Android Gradle plugin artifacts.

## [0.1.5] - 2026-10-12

- Resolve Android Gradle plugin IDs directly and use Maven Central's Apache endpoint to avoid plugin marker rate limits.

## [0.1.4] - 2026-10-12

- Add a direct Maven Central endpoint for plugin resolution in JitPack's build environment.

## [0.1.3] - 2026-10-12

- Resolve the Compose Gradle plugin directly from its Maven artifact for reliable JitPack builds.

## [0.1.2] - 2026-10-12

- Remove the Foojay toolchain resolver from settings so JitPack can configure the build with its provided JDK 21.

## [0.1.1] - 2026-10-12

- Reduce the default toast elevation from 12dp to 6dp.
- Fix swipe dismissal so the toast continues in the selected gesture direction instead of snapping back.

## [0.1.0] - 2026-10-12

- First public release of Floaty, a Compose Multiplatform in-app toast library.
- Add queued notifications with semantic styles, actions, progress, custom content, and programmatic dismissal.
- Add nine screen positions, safe drawing insets, theme-aware styling, motion, and customizable gesture dismissal.
- Add Android, iOS, desktop, JavaScript, and WasmJS target publications.
- Add GitHub Actions CI and JitPack release verification.
