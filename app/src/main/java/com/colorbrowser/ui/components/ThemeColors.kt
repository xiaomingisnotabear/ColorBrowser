package com.colorbrowser.ui.components

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 全局深色状态。
 * 由 MainActivity 提供，使手动切换的夜间模式能够覆盖系统设置。
 */
val LocalAppDark = staticCompositionLocalOf { false }

@Composable
fun appIsDark(): Boolean = LocalAppDark.current

val ColorBrowserLightColors = lightColorScheme(
    primary = Color(0xFF0081FF),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF0081FF),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFEFF2F6),
    onSurfaceVariant = Color(0xFF5F656E),
    outline = Color(0xFFD8DEE6)
)

val ColorBrowserDarkColors = darkColorScheme(
    primary = Color(0xFF4DA9FF),
    onPrimary = Color(0xFF00131F),
    secondary = Color(0xFF4DA9FF),
    onSecondary = Color(0xFF00131F),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFEDEDF0),
    surface = Color(0xFF2C2B30),
    onSurface = Color(0xFFEDEDF0),
    surfaceVariant = Color(0xFF37363C),
    onSurfaceVariant = Color(0xFFBFBFC4),
    outline = Color(0xFF4A4950)
)

// ===== 语义色 =====

@Composable
fun themePrimary(): Color =
    if (LocalAppDark.current) Color(0xFF4DA9FF) else Color(0xFF0081FF)

@Composable
fun themeBackground(): Color =
    if (LocalAppDark.current) Color(0xFF1C1B1F) else Color(0xFFF5F7FA)

@Composable
fun themeSurface(): Color =
    if (LocalAppDark.current) Color(0xFF2C2B30) else Color(0xFFFFFFFF)

@Composable
fun themeTextPrimary(): Color =
    if (LocalAppDark.current) Color(0xFFEDEDF0) else Color(0xFF1A1A1A)

@Composable
fun themeTextSecondary(): Color =
    if (LocalAppDark.current) Color(0xFFBFBFC4) else Color(0xFF9E9E9E)

/** 液态亚克力底色：半透明，让下层内容透出形成层叠感 */
@Composable
fun glassSurfaceColor(): Color =
    if (LocalAppDark.current) Color(0xF22C2B30) else Color(0xF2FFFFFF)

/** 玻璃卡片顶部高光描边 */
@Composable
fun glassStrokeColor(): Color =
    if (LocalAppDark.current) Color(0x26FFFFFF) else Color(0x66FFFFFF)

/** 输入框等弱化容器底色 */
@Composable
fun subtleFillColor(): Color =
    if (LocalAppDark.current) Color(0x1AFFFFFF) else Color(0x0D000000)
