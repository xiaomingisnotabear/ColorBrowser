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
import com.colorbrowser.ui.components.*

/**
 * 下载管理界面
 */
@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val downloads = remember {
        listOf(
            DownloadItem("sample.pdf", "https://example.com/sample.pdf", "2.3 MB", "完成", true),
            DownloadItem("image.png", "https://example.com/image.png", "1.5 MB", "完成", true),
            DownloadItem("video.mp4", "https://example.com/video.mp4", "45 MB", "下载中", false)
        )
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
                text = "下载",
                style = MaterialTheme.typography.titleLarge,
                color = if (dark) Color(0xFFEDEDF0) else Color(0xFF1A1A1A),
                modifier = Modifier.weight(1f)
            )
        }

        // 下载列表
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(downloads.size) { index ->
                val item = downloads[index]
                DownloadItemRow(
                    item = item,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 下载项数据类
 */
data class DownloadItem(
    val fileName: String,
    val url: String,
    val size: String,
    val status: String,
    val isComplete: Boolean
)

/**
 * 单个下载项行
 */
@Composable
fun DownloadItemRow(
    item: DownloadItem,
    modifier: Modifier = Modifier
) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()

    GlassSurface(modifier = modifier, cornerRadius = 16.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 文件图标
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Filled.Description,
                contentDescription = item.fileName,
                tint = if (dark) Color(0xFFBFBFC4) else Color(0xFF9E9E9E),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))

            // 文件信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (dark) Color(0xFFEDEDF0) else Color(0xFF1A1A1A)
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.size,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (dark) Color(0xFFBFBFC4) else Color(0xFF9E9E9E)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (item.isComplete) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }
            }

            // 操作按钮
            if (!item.isComplete) {
                IconButton(onClick = { /* 暂停/继续 */ }) {
                    Icon(
                        imageVector = if (item.status == "下载中")
                            androidx.compose.material.icons.Icons.Filled.Pause
                        else
                            androidx.compose.material.icons.Icons.Filled.PlayArrow,
                        contentDescription = "暂停",
                        tint = if (dark) Color(0xFFBFBFC4) else Color(0xFF9E9E9E)
                    )
                }
            }
        }
    }
}
