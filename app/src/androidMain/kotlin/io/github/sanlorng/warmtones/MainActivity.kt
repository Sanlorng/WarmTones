package io.github.sanlorng.warmtones

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import io.github.sanlorng.warmtones.data.SettingsRepository
import io.github.sanlorng.warmtones.ui.screen.WarmTonesApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WarmTonesApp()
        }

        val preDrawListener = ViewTreeObserver.OnPreDrawListener { false }

        val contentView = findViewById<View>(android.R.id.content)
            contentView.viewTreeObserver.addOnPreDrawListener(
                preDrawListener
            )

        lifecycleScope.launch {
            SettingsRepository(this@MainActivity)
                .isPagerModeEnabled.first()
            contentView.viewTreeObserver.removeOnPreDrawListener(preDrawListener)
        }

    }
}
