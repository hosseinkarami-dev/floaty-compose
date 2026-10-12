package io.github.hosseinkarami_dev.floaty

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Floaty",
    ) {
        App()
    }
}