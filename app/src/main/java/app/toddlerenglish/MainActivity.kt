package app.toddlerenglish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.toddlerenglish.ui.AppNavHost
import app.toddlerenglish.ui.theme.ToddlerEnglishTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as ToddlerApplication).container
        setContent {
            ToddlerEnglishTheme {
                AppNavHost(container)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (application as ToddlerApplication).container.audio.stop()
    }
}
