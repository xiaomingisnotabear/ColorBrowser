package com.colorbrowser.core

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import java.util.Locale

/** 一条下载记录（来自系统 DownloadManager） */
data class DownloadEntry(
    val id: Long,
    val fileName: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val status: Int
) {
    val isCompleted: Boolean get() = status == DownloadManager.STATUS_SUCCESSFUL

    val isFailed: Boolean
        get() = status == DownloadManager.STATUS_FAILED

    val isRunning: Boolean
        get() = status == DownloadManager.STATUS_RUNNING ||
            status == DownloadManager.STATUS_PENDING ||
            status == DownloadManager.STATUS_PAUSED

    val progress: Float
        get() = when {
            isCompleted -> 1f
            totalBytes > 0L -> (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
            else -> 0f
        }

    val statusText: String
        get() = when {
            isCompleted -> "已完成"
            isFailed -> "下载失败"
            status == DownloadManager.STATUS_PAUSED -> "已暂停"
            status == DownloadManager.STATUS_PENDING -> "等待中"
            else -> "${(progress * 100).toInt()}%"
        }

    val sizeText: String
        get() = formatSize(if (totalBytes > 0L) totalBytes else downloadedBytes)
}

fun formatSize(bytes: Long): String {
    if (bytes <= 0L) return "未知大小"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        else -> String.format(Locale.US, "%.0f KB", kb)
    }
}

/** 封装 DownloadManager 的查询 / 打开 / 删除 */
class DownloadRepository(private val context: Context) {

    private fun manager(): DownloadManager? =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager

    fun query(): List<DownloadEntry> {
        val dm = manager() ?: return emptyList()
        val result = mutableListOf<DownloadEntry>()
        runCatching {
            val cursor = dm.query(DownloadManager.Query())
            cursor.use { c ->
                if (c.moveToFirst()) {
                    val idIdx = c.getColumnIndex(DownloadManager.COLUMN_ID)
                    val titleIdx = c.getColumnIndex(DownloadManager.COLUMN_TITLE)
                    val totalIdx = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val doneIdx = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val statusIdx = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    do {
                        if (idIdx >= 0 && statusIdx >= 0) {
                            result += DownloadEntry(
                                id = c.getLong(idIdx),
                                fileName = if (titleIdx >= 0) c.getString(titleIdx) ?: "未命名文件" else "未命名文件",
                                totalBytes = if (totalIdx >= 0) c.getLong(totalIdx) else -1L,
                                downloadedBytes = if (doneIdx >= 0) c.getLong(doneIdx) else 0L,
                                status = c.getInt(statusIdx)
                            )
                        }
                    } while (c.moveToNext())
                }
            }
        }
        return result.sortedByDescending { it.id }
    }

    fun open(id: Long) {
        val dm = manager() ?: return
        val uri = runCatching { dm.getUriForDownloadedFile(id) }.getOrNull() ?: return
        val mime = runCatching { dm.getMimeTypeForDownloadedFile(id) }.getOrNull() ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun remove(id: Long) {
        runCatching { manager()?.remove(id) }
    }
}
