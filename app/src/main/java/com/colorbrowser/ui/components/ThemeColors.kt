package com.colorbrowser.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

// 重新导出主题颜色供外部使用
@Composable
fun themePrimary(): Color {
    val dark = isSystemInDarkTheme()
    return if (dark) Color(0xFF4DA9FF) else Color(0xFF0081FF)
}

@Composable
fun themeBackground(): Color {
    val dark = isSystemInDarkTheme()
    return if (dark) Color(0xFF1C1B1F) else Color(0xFFF5F7FA)
}

@Composable
fun themeSurface(): Color {
    val dark = isSystemInDarkTheme()
    return if (dark) Color(0xFF2C2B30) else Color(0xFFFFFFFF)
}

@Composable
fun themeTextPrimary(): Color {
    val dark = isSystemInDarkTheme()
    return if (dark) Color(0xFFEDEDF0) else Color(0xFF1A1A1A)
}

@Composable
fun themeTextSecondary(): Color {
    val dark = isSystemInDarkTheme()
    return if (dark) Color(0xFFBFBFC4) else Color(0xFF9E9E9E)
}

@Composable
fun glassSurfaceColor(): Color {
    val dark = isSystemInDarkTheme()
    return if (dark) Color(0x992C2B30) else Color(0xCCFFFFFF)
}
