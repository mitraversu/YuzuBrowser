package org.daidai.browser

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.daidai.browser.ui.DaidaiApp
import org.daidai.browser.ui.theme.DaidaiTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DaidaiTheme {
                DaidaiApp()
            }
        }
        handleViewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleViewIntent(intent)
    }

    private fun handleViewIntent(intent: Intent?) {
        val url = intent?.dataString ?: return
        if (url.startsWith("http://") || url.startsWith("https://")) {
            (application as DaidaiApplication).container.pendingUrl.tryEmit(url)
        }
    }
}
