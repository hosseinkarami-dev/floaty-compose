# API and customization guide

This guide describes Floaty's current API. Public names are in `io.github.hosseinkarami_dev.floaty`.

## State and queue

Create state with `rememberToastState(maxVisible = 3)` inside composition. The state is scoped to that composition and can be passed to callbacks. `show(message)` returns the message ID. Use an explicit `ToastMessage.id` when a later event needs to update the same message; otherwise Floaty creates an ID. `dismiss(id)` removes one message and `dismissAll()` clears the queue.

`maxVisible` limits the rendered prefix. Additional entries remain in FIFO order. Timeouts begin when a toast is composed as visible, so waiting messages do not expire in the queue. Re-showing the same ID replaces its message at its existing queue position.

## Message fields

| Field | Purpose |
| --- | --- |
| `title` | Main message text |
| `description` | Optional supporting text |
| `kind` | Semantic appearance (`Default`, `Success`, `Error`, `Warning`, `Info`, `Loading`) |
| `duration` | Auto-dismiss policy |
| `action` | One optional action button and callback |
| `dismissible` | Whether the built-in close button and gestures may dismiss it |
| `accent` | Optional per-message accent color |
| `id` | Optional stable ID for update/dismiss control |
| `progress` | Optional determinate progress from `0f` to `1f` |
| `content` | Optional composable replacing the standard message layout |

Use `ToastDuration.Indefinite` for work that needs an explicit completion or dismissal. `ToastDuration.Custom(milliseconds)` accepts zero or greater. `dismissible = false` disables user dismissal, but app code can still call `dismiss(id)`.

## Host configuration

Pass one `ToastHost(state, modifier, config)` at the desired overlay layer. It is commonly a full-size sibling after app content in a root `Box`. `ToastConfig` contains:

- `position`: all nine combinations of top/center/bottom and start/center/end. Start/end follow layout direction, so they map to left/right in LTR and reverse in RTL.
- `spacing`, `horizontalPadding`, `verticalPadding`, and `maxWidth`.
- `surfaceColor`, `contentColor`, `borderColor`, and `elevation`.
- `showDismissButton` and accessible `dismissActionLabel`.
- `respectSafeDrawingInsets`, enabled by default.
- `swipeToDismiss`, `swipeDirection`, and `swipeThreshold`.
- `animationDurationMillis`; use zero for no entrance/exit motion.

Null colors use Material 3 theme colors. `ToastMessage.accent` changes the mark, progress indicator, and action color for just that message.

## Gesture behavior

Swipes are disabled by default. Enabling `swipeToDismiss` adds an axis-locked gesture to dismissible items. `Horizontal` and its explicit directional forms only translate horizontally. `Vertical` translates toward the closest vertical edge (up from top, down from bottom); at a vertically centered position it can move either way. `Any` selects an axis from the initial drag; vertical movement follows the same position rule. `Left`, `Right`, `Up`, `Down`, `StartToEnd`, and `EndToStart` constrain the allowed direction. Start/end use the active layout direction. A gesture below `swipeThreshold` springs back; a qualifying gesture triggers the exit animation and removes the item.

## Custom content

When `content` is provided, it replaces title, description, action, and built-in progress layout. The host surface and dismissal controls remain. Build any required controls inside the content and provide meaningful Compose semantics. Keep message state in `ToastState` so it can still be removed programmatically.

## Recommended patterns

- Use `Indefinite` for uploads, synchronization, and other ongoing tasks.
- Update a running message with its stable ID, then change its kind/text and duration on completion.
- Prefer short, actionable copy; move longer explanations into the screen itself.
- Use one root host per app/window and position it above the content it overlays.
- If the parent already applies safe area insets, set `respectSafeDrawingInsets = false` to avoid double padding.

## Sample locations

The demo app is in `shared/src/commonMain`. It includes standard semantic messages, persistent progress, actions, custom content, a control that cycles through all nine positions, and controls for gesture direction and the close button.
