package com.colorbrowser.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.colorbrowser.ui.components.ColorBrowserDarkColors
import com.colorbrowser.ui.components.ColorBrowserLightColors
import com.colorbrowser.ui.components.LocalAppDark
import com.colorbrowser.ui.screens.BrowserScreen

class MainActivity : ComponentActivity() {

    /** 从外部（其它 App 点击链接）传入、待浏览器消费的地址 */
    private val pendingUrl = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingUrl.value = extractUrl(intent)
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        setContent {
            var nightMode by remember {
                mutableIntStateOf(prefs.getInt(KEY_NIGHT_MODE, 0))
            }
            val systemDark = isSystemInDarkTheme()
            val dark = when (nightMode) {
                1 -> false
                2 -> true
                else -> systemDark
            }
            val background = if (dark) Color(0xFF1C1B1F) else Color(0xFFF5F7FA)

            val view = LocalView.current
            SideEffect {
                val window = (view.context as? Activity)?.window ?: return@SideEffect
                window.statusBarColor = background.toArgb()
                window.navigationBarColor = background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }

            CompositionLocalProvider(LocalAppDark provides dark) {
                MaterialTheme(
                    colorScheme = if (dark) ColorBrowserDarkColors else ColorBrowserLightColors
                ) {
                    Surface(modifier = Modifier.fillMaxSize(), color = background) {
                        BrowserScreen(
                            nightMode = nightMode,
                            onNightModeChange = { value ->
                                nightMode = value
                                prefs.edit().putInt(KEY_NIGHT_MODE, value).apply()
                            },
                            initialUrl = pendingUrl.value,
                            onUrlConsumed = { pendingUrl.value = null }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractUrl(intent)?.let { pendingUrl.value = it }
    }

    private fun extractUrl(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_VIEW) return null
        val data = intent.dataString ?: return null
        return data.takeIf {
            it.startsWith("http://") || it.startsWith("https://")
        }
    }

    companion object {
        const val PREFS_NAME = "color_browser_prefs"
        const val KEY_NIGHT_MODE = "night_mode"
    }
}
