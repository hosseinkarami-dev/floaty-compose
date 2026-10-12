package io.github.hosseinkarami_dev.floaty

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hosseinkarami_dev.floaty.ToastAction
import io.github.hosseinkarami_dev.floaty.ToastConfig
import io.github.hosseinkarami_dev.floaty.ToastDuration
import io.github.hosseinkarami_dev.floaty.ToastHost
import io.github.hosseinkarami_dev.floaty.ToastKind
import io.github.hosseinkarami_dev.floaty.ToastMessage
import io.github.hosseinkarami_dev.floaty.ToastPosition
import io.github.hosseinkarami_dev.floaty.ToastSwipeDirection
import io.github.hosseinkarami_dev.floaty.rememberToastState
import io.github.hosseinkarami_dev.floaty.showError
import io.github.hosseinkarami_dev.floaty.showInfo
import io.github.hosseinkarami_dev.floaty.showSuccess
import io.github.hosseinkarami_dev.floaty.showWarning

/** Static sample surface used by platform previews to compare Floaty's light and dark rendering. */
@Composable
fun FloatyThemePreview(darkMode: Boolean) {
    val toastState = rememberToastState(maxVisible = 2)
    LaunchedEffect(toastState) {
        toastState.showSuccess("Changes saved", "Your work is up to date.")
        toastState.show(ToastMessage(
            title = "Uploading your file",
            description = "Brand guidelines.pdf · 62%",
            kind = ToastKind.Loading,
            duration = ToastDuration.Indefinite,
            progress = .62f,
        ))
    }
    MaterialTheme(colorScheme = if (darkMode) darkColorScheme() else lightColorScheme()) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Floaty · ${if (darkMode) "Dark" else "Light"} theme", style = MaterialTheme.typography.titleLarge)
                ToastHost(
                    toastState,
                    Modifier.fillMaxSize(),
                    ToastConfig(position = ToastPosition.TopEnd, showDismissButton = false),
                )
            }
        }
    }
}

@Composable
fun App(initialDarkMode: Boolean = false) {
    val toastState = rememberToastState(maxVisible = 3)
    var darkMode by remember { mutableStateOf(initialDarkMode) }
    var swipeEnabled by remember { mutableStateOf(true) }
    var closeButtonVisible by remember { mutableStateOf(false) }
    var swipeDirection by remember { mutableStateOf(ToastSwipeDirection.Horizontal) }
    var toastPosition by remember { mutableStateOf(ToastPosition.TopEnd) }
    val config = remember(swipeEnabled, closeButtonVisible, swipeDirection, toastPosition) {
        ToastConfig(
            position = toastPosition,
            showDismissButton = closeButtonVisible,
            swipeToDismiss = swipeEnabled,
            swipeDirection = swipeDirection,
            swipeThreshold = 72.dp,
        )
    }
    MaterialTheme(colorScheme = if (darkMode) darkColorScheme() else lightColorScheme()) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                Modifier.fillMaxSize().safeContentPadding().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Floaty", style = MaterialTheme.typography.headlineLarge)
                    OutlinedButton(onClick = { darkMode = !darkMode }) {
                        Text(if (darkMode) "Light mode" else "Dark mode")
                    }
                }
                Text("A little lift for useful updates", modifier = Modifier.padding(top = 6.dp, bottom = 24.dp))
                Text("Try swiping a notification to dismiss it.", modifier = Modifier.padding(bottom = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { swipeEnabled = !swipeEnabled }) {
                        Text(if (swipeEnabled) "Swipe: on" else "Swipe: off")
                    }
                    OutlinedButton(onClick = { closeButtonVisible = !closeButtonVisible }) {
                        Text(if (closeButtonVisible) "Close button: on" else "Close button: off")
                    }
                }
                OutlinedButton(onClick = {
                    swipeDirection = if (swipeDirection == ToastSwipeDirection.Horizontal) ToastSwipeDirection.Vertical
                    else ToastSwipeDirection.Horizontal
                }) {
                    Text("Swipe direction: ${swipeDirection.name.lowercase()}")
                }
                OutlinedButton(onClick = {
                    val positions = ToastPosition.values()
                    toastPosition = positions[(positions.indexOf(toastPosition) + 1) % positions.size]
                }) {
                    Text("Position: ${toastPosition.name}")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { toastState.showSuccess("Changes saved", "Your work is up to date.") }) { Text("Success") }
                    Button(onClick = { toastState.showError("Upload failed", "Check your connection and try again.") }) { Text("Error") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { toastState.showInfo("New update", "Version 2.4 is ready.") }) { Text("Info") }
                    OutlinedButton(onClick = { toastState.showWarning("Unsaved changes") }) { Text("Warning") }
                }
                OutlinedButton(onClick = {
                    toastState.show(ToastMessage(
                        title = "Item deleted",
                        description = "You can still restore it.",
                        action = ToastAction("Undo") { toastState.showSuccess("Restored") },
                        duration = ToastDuration.Long,
                    ))
                }) { Text("Show action") }
                OutlinedButton(onClick = {
                    toastState.show(ToastMessage("Working…", "This stays until dismissed.", ToastKind.Loading, ToastDuration.Indefinite))
                }) { Text("Indefinite loading") }
                OutlinedButton(onClick = {
                    toastState.show(ToastMessage(
                        title = "Custom notification",
                        dismissible = false,
                        content = { Text("This layout is supplied by the app.", style = MaterialTheme.typography.bodyMedium) },
                    ))
                }) { Text("Custom content") }
                OutlinedButton(onClick = {
                    toastState.show(ToastMessage(
                        title = "Uploading",
                        description = "Preparing your file",
                        kind = ToastKind.Loading,
                        duration = ToastDuration.Indefinite,
                        id = "sample-upload",
                        progress = .62f,
                    ))
                }) { Text("Progress") }
                OutlinedButton(onClick = { toastState.dismiss("sample-upload") }) { Text("Dismiss progress") }
                OutlinedButton(onClick = { toastState.dismissAll() }) { Text("Dismiss all") }
            }
        }
        ToastHost(toastState, Modifier.fillMaxSize(), config)
    }
}
