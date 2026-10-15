package com.colorbrowser.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.colorbrowser.core.TabManager
import com.colorbrowser.ui.components.*

/**
 * 设置界面
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    tabManager: TabManager,
    modifier: Modifier = Modifier
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()

    // 偏好设置状态
    var nightMode by remember {
        mutableStateOf(tabManager.getBooleanPreference("night_mode", false))
    }
    var blockPopups by remember {
        mutableStateOf(tabManager.getBooleanPreference("block_popups", true))
    }
    var blockAds by remember {
        mutableStateOf(tabManager.getBooleanPreference("block_ads", false))
    }
    var fontSize by remember {
        mutableStateOf(tabManager.getFontSize())
    }
    var userAgentMobile by remember {
        mutableStateOf(tabManager.getBooleanPreference("user_agent_mobile", true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (dark) Color(0xFF1C1B1F) else Color(0xFFF5F7FA))
    ) {
        // 标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = if (dark) Color(0xFFEDEDF0) else Color(0xFF1A1A1A)
                )
            }
            Text(
                text = "设置",
                style = MaterialTheme.typography.titleLarge,
                color = if (dark) Color(0xFFEDEDF0) else Color(0xFF1A1A1A),
                modifier = Modifier.weight(1f)
            )
        }

        // 设置列表
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 外观
            item {
                SettingsSection(title = "外观") {
                    // 夜间模式
                    SettingsToggleRow(
                        title = "夜间模式",
                        subtitle = "切换深色主题",
                        checked = nightMode,
                        onCheckedChange = {
                            nightMode = it
                            tabManager.setBooleanPreference("night_mode", it)
                        }
                    )
                }
            }

            // 隐私与安全
            item {
                SettingsSection(title = "隐私与安全") {
                    SettingsToggleRow(
                        title = "阻止弹出窗口",
                        subtitle = "拦截网页弹窗",
                        checked = blockPopups,
                        onCheckedChange = {
                            blockPopups = it
                            tabManager.setBooleanPreference("block_popups", it)
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    SettingsToggleRow(
                        title = "拦截广告",
                        subtitle = "过滤广告加载",
                        checked = blockAds,
                        onCheckedChange = {
                            blockAds = it
                            tabManager.setBooleanPreference("block_ads", it)
                        }
                    )
                }
            }

            // 阅读
            item {
                SettingsSection(title = "阅读") {
                    // 字体大小
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "字体大小",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (dark) Color(0xFFEDEDF0) else Color(0xFF1A1A1A)
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = fontSize.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (dark) Color(0xFFBFBFC4) else Color(0xFF9E9E9E)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Slider(
                            value = fontSize.toFloat(),
                            onValueChange = {
                                fontSize = it.toInt()
                                tabManager.setFontSize(it.toInt())
                            },
                            valueRange = 50f..200f,
                            steps = 4,
                            colors = SliderDefaults.colors(
                                thumbColor = themePrimary(),
                                activeTrackColor = themePrimary()
                            )
                        )
                    }

                    // 用户代理
                    SettingsChoiceRow(
                        title = "用户代理",
                        subtitle = if (userAgentMobile) "移动端" else "桌面端"
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilterChip(
                                selected = userAgentMobile,
                                onClick = {
                                    userAgentMobile = true
                                    tabManager.setBooleanPreference("user_agent_mobile", true)
                                },
                                label = { Text("移动端") }
                            )
                            FilterChip(
                                selected = !userAgentMobile,
                                onClick = {
                                    userAgentMobile = false
                                    tabManager.setBooleanPreference("user_agent_mobile", false)
                                },
                                label = { Text("桌面端") }
                            )
                        }
                    }
                }
            }

            // 数据
            item {
                SettingsSection(title = "数据") {
                    SettingsClearCacheRow(
                        title = "清除缓存",
                        subtitle = "删除浏览器缓存数据"
                    ) {
                        // 清除缓存操作
                    }
                }
            }

            // 关于
            item {
                SettingsSection(title = "关于") {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "版本",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (dark) Color(0xFFEDEDF0) else Color(0xFF1A1A1A)
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "1.0.0",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (dark) Color(0xFFBFBFC4) else Color(0xFF9E9E9E)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 设置分组容器
 */
@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()

    GlassSurface(cornerRadius = 16.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (dark) Color(0xFFBFBFC4) else Color(0xFF9E9E9E)
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

/**
 * 开关行
 */
@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (dark) Color(0xFFEDEDF0) else Color(0xFF1A1A1A)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (dark) Color(0xFFBFBFC4) else Color(0xFF9E9E9E)
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

/**
 * 选择行
 */
@Composable
fun SettingsChoiceRow(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (dark) Color(0xFFEDEDF0) else Color(0xFF1A1A1A)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (dark) Color(0xFFBFBFC4) else Color(0xFF9E9E9E)
            )
        }
        content()
    }
}

/**
 * 清除缓存行
 */
@Composable
fun SettingsClearCacheRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (dark) Color(0xFFEDEDF0) else Color(0xFF1A1A1A)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (dark) Color(0xFFBFBFC4) else Color(0xFF9E9E9E)
            )
        }
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Filled.Delete,
            contentDescription = "清除",
            tint = if (dark) Color(0xFFBFBFC4) else Color(0xFF9E9E9E)
        )
    }
}
