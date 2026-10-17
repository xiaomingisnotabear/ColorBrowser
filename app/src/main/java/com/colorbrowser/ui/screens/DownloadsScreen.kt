package com.colorbrowser.ui.screens

import android.content.Context
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.colorbrowser.core.DownloadEntry
import com.colorbrowser.core.DownloadRepository
import com.colorbrowser.ui.components.BouncyIconButton
import com.colorbrowser.ui.components.GlassSurface
import com.colorbrowser.ui.components.Radius
import com.colorbrowser.ui.components.themeBackground
import com.colorbrowser.ui.components.themePrimary
import com.colorbrowser.ui.components.themeTextPrimary
import com.colorbrowser.ui.components.themeTextSecondary
import kotlinx.coroutines.delay

/**
 * 下载管理：读取系统 DownloadManager 的真实记录，2 秒轮询刷新
 */
@Composable
fun DownloadsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { DownloadRepository(context) }
    val entries = remember { mutableStateListOf<DownloadEntry>() }

    LaunchedEffect(Unit) {
        while (true) {
            val latest = repository.query()
            entries.clear()
            entries.addAll(latest)
            delay(2000)
        }
    }

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
                text = "下载",
                style = MaterialTheme.typography.titleMedium,
                color = themeTextPrimary(),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${entries.size} 项",
                style = MaterialTheme.typography.bodySmall,
                color = themeTextSecondary()
            )
            Spacer(Modifier.width(12.dp))
        }

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "暂无下载记录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = themeTextSecondary()
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "在网页中点击下载链接即可开始",
                        style = MaterialTheme.typography.bodySmall,
                        color = themeTextSecondary()
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(items = entries, key = { it.id }) { entry ->
                    DownloadRow(
                        entry = entry,
                        onOpen = { repository.open(entry.id) },
                        onRemove = {
                            repository.remove(entry.id)
                            entries.remove(entry)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(
    entry: DownloadEntry,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .clickable(enabled = entry.isCompleted, onClick = onOpen),
        cornerRadius = Radius.md,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(themePrimary().copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = themePrimary(),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = themeTextPrimary(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.sizeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = themeTextSecondary()
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = entry.statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            entry.isCompleted -> Color(0xFF3DBE7A)
                            entry.isFailed -> Color(0xFFE05B5B)
                            else -> themePrimary()
                        }
                    )
                }

                if (entry.isRunning && entry.totalBytes > 0L) {
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = entry.progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(CircleShape),
                        color = themePrimary(),
                        trackColor = themePrimary().copy(alpha = 0.18f)
                    )
                }
            }

            BouncyIconButton(
                icon = Icons.Filled.Close,
                contentDescription = "删除记录",
                onClick = onRemove,
                tint = themeTextSecondary(),
                size = 32.dp,
                iconSize = 16.dp
            )
        }
    }
}
