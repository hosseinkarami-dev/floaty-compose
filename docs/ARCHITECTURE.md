# Architecture

## Module boundaries

`:floaty` is the reusable Kotlin Multiplatform library. Its API and UI implementation live in `floaty/src/commonMain`; deterministic state tests belong in `floaty/src/commonTest`. `:shared` is the sample UI, with Android, desktop, iOS, and browser launchers in their respective app modules.

## State and rendering

`ToastState` owns an ordered, observable list of entries. `show` either appends a new ID or replaces an existing ID in place. `snapshot()` exposes at most `maxVisible` entries to the host; entries beyond that prefix remain queued. Each rendered item owns its visible state and timer. Consequently, queued messages begin their timeout only when they become visible.

`ToastHost` maps configuration to an aligned column overlay and applies safe drawing insets by default. Each item renders either the built-in semantic message or caller-provided composable content inside a themed surface. The host handles the close control and transition lifecycle. Optional swipe input is attached per dismissible item and locks movement to one axis.

## Dependencies

The public API exposes Compose Runtime, Foundation, and UI types, so these are API dependencies. Material 3 is an implementation dependency for built-in surfaces, typography, and controls. The library does not require an external icon pack.

## Design constraints

- Keep state isolated per host; there is no global singleton.
- Keep platform-independent behavior in common source sets.
- Keep queued entries from starting their timers early.
- Keep custom content inside Floaty's host surface and dismissal lifecycle.
- Avoid claiming a target is supported based only on its declaration in Gradle.

## Current limitations

State survives recomposition but is not persisted across process death. Toasts are in-app UI, not operating-system notifications. Current accessibility and gesture behavior should be verified in consuming apps on each target. JitPack Apple target publication depends on whether the tagged dependency graph can produce Apple `.klib` artifacts on Linux; final Apple binaries require macOS.
