package com.colorbrowser.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.colorbrowser.core.TabWebView
import com.colorbrowser.ui.components.BouncyIconButton
import com.colorbrowser.ui.components.Radius
import com.colorbrowser.ui.components.TabCard
import com.colorbrowser.ui.components.themeBackground
import com.colorbrowser.ui.components.themePrimary
import com.colorbrowser.ui.components.themeTextPrimary
import com.colorbrowser.ui.components.themeTextSecondary

/**
 * 标签页管理：卡片列表 + 新建入口
 */
@Composable
fun TabsScreen(
    tabs: List<TabWebView>,
    currentTabId: Int,
    onSelect: (Int) -> Unit,
    onClose: (Int) -> Unit,
    onNewTab: () -> Unit,
    onCloseAll: () -> Unit,
    onBack: () -> Unit
) {
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
                text = "标签页",
                style = MaterialTheme.typography.titleMedium,
                color = themeTextPrimary(),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${tabs.size} 个",
                style = MaterialTheme.typography.bodySmall,
                color = themeTextSecondary()
            )
            Spacer(Modifier.width(8.dp))
            Surface(
                onClick = onCloseAll,
                shape = RoundedCornerShape(Radius.sm),
                color = themeTextSecondary().copy(alpha = 0.12f)
            ) {
                Text(
                    text = "全部关闭",
                    style = MaterialTheme.typography.labelMedium,
                    color = themeTextSecondary(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(items = tabs, key = { it.tabId }) { tab ->
                TabCard(
                    title = tab.title.ifBlank { "新标签页" },
                    url = tab.url,
                    isSelected = tab.tabId == currentTabId,
                    onClick = { onSelect(tab.tabId) },
                    onClose = { onClose(tab.tabId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Surface(
                    onClick = onNewTab,
                    shape = RoundedCornerShape(Radius.md),
                    color = themePrimary().copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "新建标签页",
                            tint = themePrimary(),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "新建标签页",
                            style = MaterialTheme.typography.bodyLarge,
                            color = themePrimary()
                        )
                    }
                }
            }
        }
    }
}
