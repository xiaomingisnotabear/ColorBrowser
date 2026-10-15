package com.colorbrowser.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import com.colorbrowser.ui.screens.BrowserScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            val darkTheme = isSystemInDarkTheme()
            val colors = if (darkTheme) DarkColors else LightColors

            MaterialTheme(
                colorScheme = colors,
                typography = Typography()
            ) {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = colors.background
                ) {
                    BrowserScreen()
                }
            }
        }
    }
}

val LightColors = androidx.compose.material3.LightColorScheme(
    primary = Color(0xFF0081FF),
    onPrimary = Color.White,
    secondary = Color(0xFF0081FF),
    background = Color(0xFFF5F7FA),
    surface = Color.White,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A)
)

val DarkColors = androidx.compose.material3.DarkColorScheme(
    primary = Color(0xFF4DA9FF),
    onPrimary = Color(0xFF1C1B1F),
    secondary = Color(0xFF4DA9FF),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2C2B30),
    onBackground = Color.White,
    onSurface = Color.White
)
