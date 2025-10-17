package io.github.sanlorng.warmtones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation3.runtime.NavKey
import io.github.sanlorng.warmtones.ui.screen.WarmTonesApp
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WarmTonesApp(application = application)
        }
    }
}

sealed interface Route : NavKey {

    @Serializable
    object Contacts : Route

    @Serializable
    object Settings : Route

    @Serializable
    object Pager : Route
}