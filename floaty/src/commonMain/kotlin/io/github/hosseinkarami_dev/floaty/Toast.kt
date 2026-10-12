package io.github.hosseinkarami_dev.floaty

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** The semantic style used to choose Floaty's default accent and mark. */
enum class ToastKind { Default, Success, Error, Warning, Info, Loading }

/** How long a notification remains visible. */
sealed interface ToastDuration {
    data object Short : ToastDuration
    data object Medium : ToastDuration
    data object Long : ToastDuration
    data object Indefinite : ToastDuration
    data class Custom(val milliseconds: kotlin.Long) : ToastDuration {
        init { require(milliseconds >= 0) { "Toast duration cannot be negative" } }
    }

}

private fun ToastDuration.millis(): kotlin.Long? = when (this) {
    ToastDuration.Short -> 2_000L
    ToastDuration.Medium -> 3_500L
    ToastDuration.Long -> 5_000L
    ToastDuration.Indefinite -> null
    is ToastDuration.Custom -> milliseconds
}

/** An optional action displayed alongside a notification. */
data class ToastAction(val label: String, val onClick: () -> Unit)

/** A notification to display in a [ToastHost]. */
data class ToastMessage(
    val title: String,
    val description: String? = null,
    val kind: ToastKind = ToastKind.Default,
    val duration: ToastDuration = ToastDuration.Medium,
    val action: ToastAction? = null,
    val dismissible: Boolean = true,
    val accent: Color? = null,
    val id: String? = null,
    /** Optional determinate progress in the inclusive range 0f..1f. */
    val progress: Float? = null,
    /** Replaces Floaty's built-in content layout while retaining the host surface and dismissal behavior. */
    val content: (@Composable () -> Unit)? = null,
)

private data class Entry(val id: String, val message: ToastMessage)
private enum class SwipeAxis { None, Horizontal, Vertical }

/** State and FIFO queue for one host. Create with [rememberToastState]. */
@Stable
class ToastState internal constructor(private val maxVisible: Int) {
    private val entries = mutableStateListOf<Entry>()
    private var nextId = 0L

    /** Number of currently visible notifications. */
    val visibleCount: Int get() = entries.size.coerceAtMost(maxVisible)
    /** Number of notifications waiting behind the visible items. */
    val queuedCount: Int get() = (entries.size - maxVisible).coerceAtLeast(0)

    /** Adds a notification and returns its stable identifier. */
    fun show(message: ToastMessage): String {
        require(message.progress == null || message.progress in 0f..1f) { "progress must be between 0 and 1" }
        val id = message.id ?: "floaty-${nextId++}"
        val index = entries.indexOfFirst { it.id == id }
        val entry = Entry(id, message)
        if (index >= 0) entries[index] = entry else {
            entries.add(entry)
        }
        return id
    }

    fun dismiss(id: String) { entries.removeAll { it.id == id } }
    fun dismissAll() { entries.clear() }

    internal fun snapshot(): List<Pair<String, ToastMessage>> = entries.take(maxVisible).map { it.id to it.message }
}

/** Remembers an isolated Floaty state with a bounded number of visible items. */
@Composable
fun rememberToastState(maxVisible: Int = 3): ToastState {
    require(maxVisible > 0) { "maxVisible must be greater than zero" }
    return remember(maxVisible) { ToastState(maxVisible) }
}

/** Convenient semantic helpers. */
fun ToastState.showSuccess(title: String, message: String? = null, duration: ToastDuration = ToastDuration.Medium) =
    show(ToastMessage(title, message, ToastKind.Success, duration))
fun ToastState.showError(title: String, message: String? = null, duration: ToastDuration = ToastDuration.Long) =
    show(ToastMessage(title, message, ToastKind.Error, duration))
fun ToastState.showInfo(title: String, message: String? = null, duration: ToastDuration = ToastDuration.Medium) =
    show(ToastMessage(title, message, ToastKind.Info, duration))
fun ToastState.showWarning(title: String, message: String? = null, duration: ToastDuration = ToastDuration.Medium) =
    show(ToastMessage(title, message, ToastKind.Warning, duration))

enum class ToastPosition {
    TopStart, TopCenter, TopEnd,
    CenterStart, Center, CenterEnd,
    BottomStart, BottomCenter, BottomEnd,
}

private fun ToastPosition.isTop() = this == ToastPosition.TopStart || this == ToastPosition.TopCenter || this == ToastPosition.TopEnd
private fun ToastPosition.isBottom() = this == ToastPosition.BottomStart || this == ToastPosition.BottomCenter || this == ToastPosition.BottomEnd
private fun ToastPosition.isVerticallyCentered() = this == ToastPosition.CenterStart || this == ToastPosition.Center || this == ToastPosition.CenterEnd

/** Allowed drag directions for swipe-to-dismiss. Start/end follow the current layout direction. */
enum class ToastSwipeDirection { Horizontal, Vertical, Any, Left, Right, Up, Down, StartToEnd, EndToStart }

/** Global layout and visual defaults for a host. */
data class ToastConfig(
    val position: ToastPosition = ToastPosition.TopEnd,
    val spacing: Dp = 10.dp,
    val horizontalPadding: Dp = 16.dp,
    val verticalPadding: Dp = 16.dp,
    val maxWidth: Dp = 440.dp,
    val surfaceColor: Color? = null,
    val contentColor: Color? = null,
    val borderColor: Color? = null,
    val elevation: Dp = 12.dp,
    val showDismissButton: Boolean = true,
    /** Avoid status bars, navigation bars, and display cutouts when the host platform exposes safe insets. */
    val respectSafeDrawingInsets: Boolean = true,
    /** Enables swipe-to-dismiss for dismissible notifications. Off by default. */
    val swipeToDismiss: Boolean = false,
    /** Which direction(s) can dismiss a notification when swipe-to-dismiss is enabled. */
    val swipeDirection: ToastSwipeDirection = ToastSwipeDirection.Horizontal,
    /** Minimum drag distance before a swipe dismisses. */
    val swipeThreshold: Dp = 96.dp,
    /** Label announced for the built-in dismiss control; provide a localized string when needed. */
    val dismissActionLabel: String = "Dismiss notification",
    /** Shared entrance and exit animation duration. Use zero to turn motion off. */
    val animationDurationMillis: Int = 220,
)

/** Place once near the root of your UI. Toasts are drawn as a Compose overlay. */
@Composable
fun ToastHost(
    state: ToastState,
    modifier: Modifier = Modifier,
    config: ToastConfig = ToastConfig(),
) {
    require(config.swipeThreshold > 0.dp) { "swipeThreshold must be greater than zero" }
    require(config.animationDurationMillis >= 0) { "animationDurationMillis cannot be negative" }
    val items = state.snapshot()
    val alignment = when (config.position) {
        ToastPosition.TopStart -> Alignment.TopStart
        ToastPosition.TopCenter -> Alignment.TopCenter
        ToastPosition.TopEnd -> Alignment.TopEnd
        ToastPosition.CenterStart -> Alignment.CenterStart
        ToastPosition.Center -> Alignment.Center
        ToastPosition.CenterEnd -> Alignment.CenterEnd
        ToastPosition.BottomStart -> Alignment.BottomStart
        ToastPosition.BottomCenter -> Alignment.BottomCenter
        ToastPosition.BottomEnd -> Alignment.BottomEnd
    }
    val insetModifier = if (config.respectSafeDrawingInsets) modifier.safeDrawingPadding() else modifier
    Box(insetModifier, contentAlignment = alignment) {
        Column(
            modifier = Modifier.fillMaxWidth().animateContentSize(tween(config.animationDurationMillis.coerceAtLeast(1)))
                .padding(horizontal = config.horizontalPadding, vertical = config.verticalPadding),
            horizontalAlignment = when (config.position) {
                ToastPosition.TopCenter, ToastPosition.Center, ToastPosition.BottomCenter -> Alignment.CenterHorizontally
                ToastPosition.TopStart, ToastPosition.CenterStart, ToastPosition.BottomStart -> Alignment.Start
                else -> Alignment.End
            },
            verticalArrangement = when {
                config.position.isTop() -> Arrangement.Top
                config.position.isBottom() -> Arrangement.Bottom
                else -> Arrangement.Center
            },
        ) {
            items.forEachIndexed { index, (id, message) ->
                key(id) {
                    ToastItem(id, message, config) { state.dismiss(id) }
                    if (index < items.lastIndex) Spacer(Modifier.size(config.spacing))
                }
            }
        }
    }
}

@Composable
private fun ToastItem(id: String, message: ToastMessage, config: ToastConfig, dismiss: () -> Unit) {
    val duration = message.duration.millis()
    var visible by remember(id) { mutableStateOf(true) }
    LaunchedEffect(id, duration, visible) {
        if (visible && duration != null) {
            delay(duration)
            visible = false
        }
    }
    LaunchedEffect(id, visible) {
        if (!visible) {
            delay(config.animationDurationMillis.toLong())
            dismiss()
        }
    }
    val accent = message.accent ?: when (message.kind) {
        ToastKind.Success -> Color(0xFF24A875)
        ToastKind.Error -> Color(0xFFE45C69)
        ToastKind.Warning -> Color(0xFFE5A43B)
        ToastKind.Info -> Color(0xFF5B8DEF)
        ToastKind.Loading -> Color(0xFF9270E8)
        ToastKind.Default -> MaterialTheme.colorScheme.primary
    }
    val surface = config.surfaceColor ?: MaterialTheme.colorScheme.surface
    val content = config.contentColor ?: MaterialTheme.colorScheme.onSurface
    val layoutDirection = LocalLayoutDirection.current
    val swipeEnabled = config.swipeToDismiss && message.dismissible
    val swipeModifier = if (swipeEnabled) {
        SwipeToDismissModifier(id, config.swipeDirection, config.position, config.swipeThreshold, layoutDirection) { visible = false }
    } else Modifier
    val animationSpec = tween<Float>(config.animationDurationMillis)
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec) + scaleIn(animationSpec, initialScale = .96f) +
            slideInVertically(tween(config.animationDurationMillis)) { height ->
                when {
                    config.position.isTop() -> -height / 5
                    config.position.isBottom() -> height / 5
                    else -> 0
                }
            },
        exit = fadeOut(animationSpec) + scaleOut(animationSpec, targetScale = .98f) +
            slideOutVertically(tween(config.animationDurationMillis)) { height ->
                when {
                    config.position.isTop() -> -height / 6
                    config.position.isBottom() -> height / 6
                    else -> 0
                }
            },
    ) {
        Surface(
            modifier = Modifier.widthIn(max = config.maxWidth).fillMaxWidth()
                .then(swipeModifier)
                .shadow(config.elevation, RoundedCornerShape(18.dp))
                .border(1.dp, config.borderColor ?: content.copy(alpha = .08f), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp), color = surface, contentColor = content, tonalElevation = 2.dp,
        ) {
            if (message.content != null) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { message.content.invoke() }
                    if (config.showDismissButton && message.dismissible) {
                        FloatyDismissButton(config.dismissActionLabel, content) { visible = false }
                    }
                }
            } else {
                Column {
                    Row(Modifier.padding(start = 16.dp, top = 14.dp, end = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(38.dp).background(accent.copy(alpha = .12f), CircleShape), contentAlignment = Alignment.Center) {
                            if (message.kind == ToastKind.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = accent, strokeWidth = 2.dp)
                            } else {
                                ToastMark(message.kind, accent)
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(message.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            message.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = content.copy(alpha = .76f)) }
                            message.action?.let { action ->
                                TextButton(onClick = { action.onClick(); visible = false }) { Text(action.label, color = accent) }
                            }
                        }
                        if (config.showDismissButton && message.dismissible) {
                            FloatyDismissButton(config.dismissActionLabel, content) { visible = false }
                        }
                    }
                    message.progress?.let { progress ->
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                            color = accent,
                            trackColor = accent.copy(alpha = .12f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatyDismissButton(label: String, color: Color, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp).semantics { contentDescription = label },
    ) {
        Canvas(Modifier.size(14.dp)) {
            val inset = size.minDimension * .12f
            drawLine(color.copy(alpha = .68f), Offset(inset, inset), Offset(size.width - inset, size.height - inset), 1.8.dp.toPx(), StrokeCap.Round)
            drawLine(color.copy(alpha = .68f), Offset(size.width - inset, inset), Offset(inset, size.height - inset), 1.8.dp.toPx(), StrokeCap.Round)
        }
    }
}

@Composable
private fun ToastMark(kind: ToastKind, color: Color) {
    Canvas(Modifier.size(19.dp)) {
        val stroke = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
        val w = size.width
        val h = size.height
        when (kind) {
            ToastKind.Success -> {
                drawLine(color, Offset(w * .18f, h * .52f), Offset(w * .42f, h * .75f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * .42f, h * .75f), Offset(w * .84f, h * .25f), stroke.width, StrokeCap.Round)
            }
            ToastKind.Error -> {
                drawLine(color, Offset(w * .25f, h * .25f), Offset(w * .75f, h * .75f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * .75f, h * .25f), Offset(w * .25f, h * .75f), stroke.width, StrokeCap.Round)
            }
            ToastKind.Warning -> {
                drawLine(color, Offset(w * .5f, h * .18f), Offset(w * .5f, h * .58f), stroke.width, StrokeCap.Round)
                drawCircle(color, radius = 1.2.dp.toPx(), center = Offset(w * .5f, h * .8f))
            }
            ToastKind.Info -> {
                drawLine(color, Offset(w * .5f, h * .42f), Offset(w * .5f, h * .82f), stroke.width, StrokeCap.Round)
                drawCircle(color, radius = 1.2.dp.toPx(), center = Offset(w * .5f, h * .2f))
            }
            ToastKind.Default, ToastKind.Loading -> drawCircle(color, radius = w * .22f)
        }
    }
}

@Composable
private fun SwipeToDismissModifier(
    id: String,
    direction: ToastSwipeDirection,
    position: ToastPosition,
    threshold: Dp,
    layoutDirection: LayoutDirection,
    requestDismiss: () -> Unit,
): Modifier {
    val density = LocalDensity.current
    val thresholdPx = with(density) { threshold.toPx() }
    var dragX by remember(id) { mutableFloatStateOf(0f) }
    var dragY by remember(id) { mutableFloatStateOf(0f) }
    var activeAxis by remember(id) { androidx.compose.runtime.mutableStateOf(SwipeAxis.None) }
    val animatedX by animateFloatAsState(dragX, label = "floatySwipeX")
    val animatedY by animateFloatAsState(dragY, label = "floatySwipeY")

    return Modifier
        .graphicsLayer { translationX = animatedX; translationY = animatedY }
        .pointerInput(id, direction, position, thresholdPx, layoutDirection) {
            detectDragGestures(
                onDragEnd = {
                    val shouldDismiss = when (activeAxis) {
                        SwipeAxis.Horizontal -> kotlin.math.abs(dragX) >= thresholdPx
                        SwipeAxis.Vertical -> kotlin.math.abs(dragY) >= thresholdPx
                        SwipeAxis.None -> false
                    }
                    if (shouldDismiss) requestDismiss()
                    dragX = 0f
                    dragY = 0f
                    activeAxis = SwipeAxis.None
                },
                onDragCancel = { dragX = 0f; dragY = 0f; activeAxis = SwipeAxis.None },
                onDrag = { change, amount ->
                    change.consume()
                    if (activeAxis == SwipeAxis.None) {
                        activeAxis = when (direction) {
                            ToastSwipeDirection.Horizontal,
                            ToastSwipeDirection.Left,
                            ToastSwipeDirection.Right,
                            ToastSwipeDirection.StartToEnd,
                            ToastSwipeDirection.EndToStart -> SwipeAxis.Horizontal
                            ToastSwipeDirection.Vertical,
                            ToastSwipeDirection.Up,
                            ToastSwipeDirection.Down -> SwipeAxis.Vertical
                            ToastSwipeDirection.Any -> if (kotlin.math.abs(amount.x) >= kotlin.math.abs(amount.y)) SwipeAxis.Horizontal else SwipeAxis.Vertical
                        }
                    }
                    when (activeAxis) {
                        SwipeAxis.Horizontal -> {
                            val nextX = dragX + amount.x
                            dragX = when (direction) {
                                ToastSwipeDirection.Left -> minOf(0f, nextX)
                                ToastSwipeDirection.Right -> maxOf(0f, nextX)
                                ToastSwipeDirection.StartToEnd -> if (layoutDirection == LayoutDirection.Ltr) maxOf(0f, nextX) else minOf(0f, nextX)
                                ToastSwipeDirection.EndToStart -> if (layoutDirection == LayoutDirection.Ltr) minOf(0f, nextX) else maxOf(0f, nextX)
                                else -> nextX
                            }
                            dragY = 0f
                        }
                        SwipeAxis.Vertical -> {
                            val nextY = dragY + amount.y
                            dragY = when (direction) {
                                ToastSwipeDirection.Up -> minOf(0f, nextY)
                                ToastSwipeDirection.Down -> maxOf(0f, nextY)
                                ToastSwipeDirection.Vertical, ToastSwipeDirection.Any ->
                                    when {
                                        position.isTop() -> minOf(0f, nextY)
                                        position.isBottom() -> maxOf(0f, nextY)
                                        else -> nextY
                                    }
                                else -> 0f
                            }
                            dragX = 0f
                        }
                        SwipeAxis.None -> Unit
                    }
                },
            )
        }
        .semantics(mergeDescendants = true) {
            dismiss { requestDismiss(); true }
        }
}
