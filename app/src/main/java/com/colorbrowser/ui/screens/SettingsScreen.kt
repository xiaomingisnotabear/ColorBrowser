package com.colorbrowser.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.colorbrowser.core.TabManager
import com.colorbrowser.ui.components.BouncyIconButton
import com.colorbrowser.ui.components.GlassSurface
import com.colorbrowser.ui.components.Radius
import com.colorbrowser.ui.components.subtleFillColor
import com.colorbrowser.ui.components.themeBackground
import com.colorbrowser.ui.components.themePrimary
import com.colorbrowser.ui.components.themeTextPrimary
import com.colorbrowser.ui.components.themeTextSecondary

/**
 * 设置页：外观 / 阅读 / 浏览 / 主页 / 数据 / 关于
 */
@Composable
fun SettingsScreen(
    tabManager: TabManager,
    nightMode: Int,
    onNightModeChange: (Int) -> Unit,
    onDataCleared: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var fontSize by remember { mutableIntStateOf(tabManager.getFontSize()) }
    var desktopMode by remember { mutableStateOf(tabManager.isDesktopMode()) }
    var blockPopups by remember { mutableStateOf(tabManager.isBlockPopups()) }
    var homePage by remember { mutableStateOf(tabManager.getHomePage()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeBackground())
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BouncyIconButton(
                icon = Icons.Filled.KeyboardArrowLeft,
                contentDescription = "返回",
                onClick = onBack,
                tint = themeTextPrimary()
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "设置",
                style = MaterialTheme.typography.titleMedium,
                color = themeTextPrimary()
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ===== 外观 =====
            item {
                SettingsSection("外观") {
                    SettingLabel("夜间模式", "跟随系统或强制指定")
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SegmentChip(
                            text = "跟随系统",
                            selected = nightMode == 0,
                            onClick = {
                                onNightModeChange(0)
                                tabManager.setNightMode(0)
                            }
                        )
                        SegmentChip(
                            text = "浅色",
                            selected = nightMode == 1,
                            onClick = {
                                onNightModeChange(1)
                                tabManager.setNightMode(1)
                            }
                        )
                        SegmentChip(
                            text = "深色",
                            selected = nightMode == 2,
                            onClick = {
                                onNightModeChange(2)
                                tabManager.setNightMode(2)
                            }
                        )
                    }
                }
            }

            // ===== 阅读 =====
            item {
                SettingsSection("阅读") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "字体大小",
                            style = MaterialTheme.typography.bodyLarge,
                            color = themeTextPrimary()
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "$fontSize%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = themeTextSecondary()
                        )
                    }
                    Slider(
                        value = fontSize.toFloat(),
                        onValueChange = { fontSize = it.toInt() },
                        onValueChangeFinished = { tabManager.setFontSize(fontSize) },
                        valueRange = 50f..250f,
                        colors = SliderDefaults.colors(
                            thumbColor = themePrimary(),
                            activeTrackColor = themePrimary(),
                            inactiveTrackColor = themePrimary().copy(alpha = 0.2f)
                        )
                    )
                }
            }

            // ===== 浏览 =====
            item {
                SettingsSection("浏览") {
                    ToggleRow(
                        title = "桌面版网站",
                        subtitle = "使用桌面 User-Agent 加载页面",
                        checked = desktopMode,
                        onCheckedChange = {
                            desktopMode = it
                            tabManager.setDesktopMode(it)
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    ToggleRow(
                        title = "阻止弹窗",
                        subtitle = "禁止网页自动打开新窗口",
                        checked = blockPopups,
                        onCheckedChange = {
                            blockPopups = it
                            tabManager.setBlockPopups(it)
                        }
                    )
                }
            }

            // ===== 主页 =====
            item {
                SettingsSection("主页") {
                    SettingLabel("主页地址", "点击 Home 按钮或新建标签页时打开")
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(Radius.pill))
                                .background(subtleFillColor())
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (homePage.isEmpty()) {
                                Text(
                                    text = "https://",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = themeTextSecondary()
                                )
                            }
                            BasicTextField(
                                value = homePage,
                                onValueChange = { homePage = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = themeTextPrimary()
                                ),
                                cursorBrush = SolidColor(themePrimary())
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.pill))
                                .background(themePrimary())
                                .clickable {
                                    tabManager.setHomePage(homePage)
                                    homePage = tabManager.getHomePage()
                                    Toast.makeText(context, "已保存主页", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "保存",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // ===== 数据 =====
            item {
                SettingsSection("数据") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tabManager.clearAllData()
                                onNightModeChange(0)
                                onDataCleared()
                                Toast.makeText(context, "已清除浏览数据", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "清除浏览数据",
                                style = MaterialTheme.typography.bodyLarge,
                                color = themeTextPrimary()
                            )
                            Text(
                                text = "关闭全部标签页并重置偏好设置",
                                style = MaterialTheme.typography.bodySmall,
                                color = themeTextSecondary()
                            )
                        }
                        Text(
                            text = "清除",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFE05B5B)
                        )
                    }
                }
            }

            // ===== 关于 =====
            item {
                SettingsSection("关于") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Color 浏览器",
                            style = MaterialTheme.typography.bodyLarge,
                            color = themeTextPrimary()
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = themeTextSecondary()
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "基于 WebView 的轻量浏览器 · ColorOS 流体设计",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeTextSecondary()
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = Radius.lg,
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = themeTextSecondary()
        )
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun SettingLabel(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = themeTextPrimary()
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = themeTextSecondary()
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = themeTextPrimary()
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = themeTextSecondary()
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = themePrimary()
            )
        )
    }
}

@Composable
private fun SegmentChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.sm))
            .background(if (selected) themePrimary() else subtleFillColor())
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color.White else themeTextSecondary()
        )
    }
}
