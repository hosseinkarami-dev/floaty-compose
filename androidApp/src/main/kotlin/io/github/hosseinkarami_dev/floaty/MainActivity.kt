package io.github.hosseinkarami_dev.floaty

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Preview(name = "Floaty · Light", showBackground = true)
@Composable
fun AppAndroidLightPreview() {
    FloatyThemePreview(darkMode = false)
}

@Preview(name = "Floaty · Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun AppAndroidDarkPreview() {
    FloatyThemePreview(darkMode = true)
}
