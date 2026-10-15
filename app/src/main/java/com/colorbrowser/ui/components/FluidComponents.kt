package com.colorbrowser.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.KeyboardEnterInteraction
import androidx.compose.foundation.interaction.LongPressInteraction
import androidx.compose.foundation.interaction.ScrollInteraction
import androidx.compose.foundation.interaction.TapInteraction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.compositionLocalOf

// ============ 设计令牌 ============

object Radius {
    val XS: Dp = with(LocalDensity.current) { 8.dp }
    val SM: Dp = with(LocalDensity.current) { 12.dp }
    val MD: Dp = with(LocalDensity.current) { 16.dp }
    val LG: Dp = with(LocalDensity.current) { 24.dp }
    val XL: Dp = with(LocalDensity.current) { 32.dp }
    val PILL: Dp = with(LocalDensity.current) { 999.dp }
}

object Spacing {
    val XS: Dp = with(LocalDensity.current) { 4.dp }
    val SM: Dp = with(LocalDensity.current) { 8.dp }
    val MD: Dp = with(LocalDensity.current) { 16.dp }
    val LG: Dp = with(LocalDensity.current) { 24.dp }
}

object Anim {
    const val FAST = 150
    const val NORMAL = 250
    const val SLOW = 400
}

val FluidSpring = SpringSpec<Float>(
    dampingRatio = Spring.DampingRatioLow,
    stiffness = Spring.StiffnessMediumLow
)

// ============ 主题颜色 ============

@Composable
fun glassSurfaceColor(): Color {
    val dark = isSystemInDarkTheme()
    return if (dark) Color(0x992C2B30) else Color(0xCCFFFFFF)
}

@Composable
fun themePrimary(): Color {
    val dark = isSystemInDarkTheme()
    return if (dark) Color(0xFF4DA9FF) else Color(0xFF0081FF)
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
fun themeBackground(): Color {
    val dark = isSystemInDarkTheme()
    return if (dark) Color(0xFF1C1B1F) else Color(0xFFF5F7FA)
}

// ============ 流体组件 ============

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    val surfaceColor = glassSurfaceColor()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = surfaceColor,
        contentColor = themeTextPrimary()
    ) {
        Box(modifier = Modifier.fillMaxSize()) { content() }
    }
}

@Composable
fun FloatingNavBar(
    selected: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf("主页", "标签页", "下载", "设置")
    val icons = listOf(
        Icons.Outlined.Home,
        Icons.Outlined.Tab,
        Icons.Outlined.Download,
        Icons.Outlined.Settings
    )

    GlassSurface(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        cornerRadius = 28.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, label ->
                val isSelected = selected == index
                val iconColor = if (isSelected) themePrimary() else themeTextSecondary()

                Column(
                    modifier = Modifier
                        .clickable { onSelected(index) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val scale = animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1f,
                        animationSpec = FluidSpring,
                        label = "nav_icon"
                    ).value

                    Icon(
                        imageVector = icons[index],
                        contentDescription = label,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp).scale(scale)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(text = label, style = MaterialTheme.typography.labelSmall, color = iconColor)
                }
            }
        }
    }
}
