package com.colorbrowser.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.colorbrowser.core.TabWebView
import com.colorbrowser.ui.components.*

/**
 * 标签页管理界面
 */
@Composable
fun TabsScreen(
    tabs: List<TabWebView>,
    currentIndex: Int,
    onTabClick: (Int) -> Unit,
    onTabLongPress: (Int) -> Unit,
    onNewTab: () -> Unit,
    onCloseAll: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    var showCloseDialog by remember { mutableStateOf(false) }

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
                text = "标签页 (${tabs.size})",
                style = MaterialTheme.typography.titleLarge,
                color = if (dark) Color(0xFFEDEDF0) else Color(0xFF1A1A1A),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onCloseAll) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "关闭全部",
                    tint = if (dark) Color(0xFFBFBFC4) else Color(0xFF9E9E9E)
                )
            }
        }

        // 标签列表
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(tabs.size) { index ->
                val tab = tabs[index]
                TabCard(
                    title = tab.title,
                    url = tab.url,
                    isSelected = index == currentIndex,
                    onClick = { onTabClick(index) },
                    onLongPress = { onTabLongPress(index) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 新建标签页
            item {
                Surface(
                    onClick = onNewTab,
                    shape = RoundedCornerShape(20.dp),
                    color = if (dark) Color(0x1AFFFFFF) else Color(0x0D000000)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "新建标签页",
                            tint = if (dark) Color(0xFFBFBFC4) else Color(0xFF9E9E9E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "新建标签页",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (dark) Color(0xFFBFBFC4) else Color(0xFF9E9E9E)
                        )
                    }
                }
            }
        }
    }
}
