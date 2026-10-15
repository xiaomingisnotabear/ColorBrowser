package com.colorbrowser.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

// 主题色扩展
val ThemeColors get() = if (isSystemInDarkTheme()) com.colorbrowser.ui.DarkColors else com.colorbrowser.ui.LightColors

// 圆角常量 - ColorOS 强圆角
object Radius {
    val XS = androidx.compose.ui.unit.Dp(8f)
    val SM = androidx.compose.ui.unit.Dp(12f)
    val MD = androidx.compose.ui.unit.Dp(16f)
    val LG = androidx.compose.ui.unit.Dp(24f)
    val XL = androidx.compose.ui.unit.Dp(32f)
    val FULL = androidx.compose.ui.unit.Dp(999f)
}

// 间距常量
object Spacing {
    val XS = androidx.compose.ui.unit.Dp(4f)
    val SM = androidx.compose.ui.unit.Dp(8f)
    val MD = androidx.compose.ui.unit.Dp(16f)
    val LG = androidx.compose.ui.unit.Dp(24f)
}

// 弹性动画时长
object Animation {
    val FAST = 200L
    val NORMAL = 300L
    val SLOW = 500L
}

// 流体形变幅度
object Fluid {
    val MAX_SCALE = 1.03f
    val MIN_SCALE = 0.97f
}
