package com.colorbrowser.core

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * 单个标签页的 WebView 封装。
 *
 * 负责：页面渲染、导航状态、标题/图标、下载拦截。
 * 通过 [onStateChanged] 通知 UI 刷新（WebView 回调均在主线程）。
 */
class TabWebView(
    val context: Context,
    val tabId: Int
) {
    val webView: WebView = WebView(context)

    var title: String = "新标签页"
        private set
    var url: String = ""
        private set
    var progress: Int = 0
        private set
    var isLoading: Boolean = false
        private set
    var canGoBack: Boolean = false
        private set
    var canGoForward: Boolean = false
        private set
    var favicon: Bitmap? = null
        private set

    /** UI 侧注册后，任意状态变化都会触发一次回调 */
    var onStateChanged: (() -> Unit)? = null

    private var destroyed = false

    init {
        setup()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setup() {
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadsImagesAutomatically = true
            mediaPlaybackRequiresUserGesture = false
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            textZoom = 100
        }

        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false

        runCatching {
            CookieManager.getInstance().apply {
                setAcceptCookie(true)
                setAcceptThirdPartyCookies(webView, true)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progress = newProgress
                isLoading = newProgress in 0..99
                notifyChanged()
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                this@TabWebView.title = title?.takeIf { it.isNotBlank() }
                    ?: this@TabWebView.url.ifBlank { "新标签页" }
                notifyChanged()
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                favicon = icon
                notifyChanged()
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, u: String?, favicon: Bitmap?) {
                this@TabWebView.url = u ?: ""
                isLoading = true
                notifyChanged()
            }

            override fun onPageFinished(view: WebView?, u: String?) {
                this@TabWebView.url = u ?: ""
                this@TabWebView.title = this@TabWebView.title.takeIf { it.isNotBlank() }
                    ?: this@TabWebView.url
                isLoading = false
                progress = 100
                updateNavigationState()
                notifyChanged()
            }

            override fun doUpdateVisitedHistory(view: WebView?, u: String?, isReload: Boolean) {
                this@TabWebView.url = u ?: ""
                updateNavigationState()
                notifyChanged()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val uri = request?.url ?: return false
                return when (uri.scheme?.lowercase()) {
                    null, "http", "https", "about", "javascript" -> false
                    else -> {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, uri)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }.onFailure { Log.w(TAG, "无法打开外部链接: $uri", it) }
                        true
                    }
                }
            }
        }

        webView.setDownloadListener(
            DownloadListener { link, userAgent, disposition, mime, length ->
                startDownload(link, userAgent, disposition, mime, length)
            }
        )
    }

    private fun notifyChanged() {
        onStateChanged?.invoke()
    }

    /** 应用用户设置（字体缩放 / 桌面 UA / 弹窗策略 / 深色底色） */
    fun applyPreferences(
        fontSize: Int,
        desktopMode: Boolean,
        blockPopups: Boolean,
        dark: Boolean
    ) {
        if (destroyed) return
        runCatching {
            webView.settings.textZoom = fontSize
            webView.settings.userAgentString =
                if (desktopMode) DESKTOP_UA
                else WebSettings.getDefaultUserAgent(context)
            webView.settings.javaScriptCanOpenWindowsAutomatically = !blockPopups
            webView.setBackgroundColor(if (dark) 0xFF1C1B1F.toInt() else 0xFFFFFFFF.toInt())
            notifyChanged()
        }
    }

    fun loadUrl(rawUrl: String) {
        if (destroyed) return
        val normalized = normalizeUrl(rawUrl)
        if (normalized.isNotEmpty()) webView.loadUrl(normalized)
    }

    fun search(query: String) {
        if (destroyed) return
        webView.loadUrl("$SEARCH_BASE${Uri.encode(query)}")
    }

    fun goBack(): Boolean {
        if (destroyed) return false
        return if (webView.canGoBack()) {
            webView.goBack()
            updateNavigationState()
            notifyChanged()
            true
        } else {
            updateNavigationState()
            false
        }
    }

    fun goForward(): Boolean {
        if (destroyed) return false
        return if (webView.canGoForward()) {
            webView.goForward()
            updateNavigationState()
            notifyChanged()
            true
        } else {
            updateNavigationState()
            false
        }
    }

    fun reload() {
        if (!destroyed) webView.reload()
    }

    fun stopLoading() {
        if (!destroyed) webView.stopLoading()
    }

    fun updateNavigationState() {
        if (destroyed) return
        canGoBack = webView.canGoBack()
        canGoForward = webView.canGoForward()
    }

    private fun startDownload(
        link: String?,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long
    ) {
        val target = link ?: return
        if (target.startsWith("blob:") || target.startsWith("data:")) {
            Log.w(TAG, "无法下载 blob/data 链接")
            return
        }
        runCatching {
            val fileName = URLUtil.guessFileName(target, contentDisposition, mimeType)
            val request = DownloadManager.Request(Uri.parse(target)).apply {
                setMimeType(mimeType)
                setTitle(fileName)
                setDescription(Uri.parse(target).host ?: "下载中")
                setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                userAgent?.let { addRequestHeader("User-Agent", it) }
                CookieManager.getInstance().getCookie(target)
                    ?.let { addRequestHeader("Cookie", it) }
            }
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
        }.onFailure { Log.e(TAG, "创建下载任务失败: $target", it) }
    }

    fun destroy() {
        if (destroyed) return
        destroyed = true
        runCatching {
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.removeAllViews()
            webView.destroy()
        }
    }

    companion object {
        private const val TAG = "TabWebView"

        /** 默认搜索引擎，可后续做成设置项 */
        private const val SEARCH_BASE = "https://www.baidu.com/s?wd="

        private const val DESKTOP_UA =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        private val DOMAIN_REGEX =
            Regex("^(?!(?:\\d+\\.)+)(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(?::\\d+)?(/.*)?$")

        /**
         * 地址栏输入规范化：
         * - 已带协议 → 原样
         * - 形如域名 → 补 https://
         * - 其它 → 搜索
         */
        fun normalizeUrl(input: String): String {
            val trimmed = input.trim()
            if (trimmed.isEmpty()) return ""
            if (trimmed.startsWith("http://") ||
                trimmed.startsWith("https://") ||
                trimmed.startsWith("file://") ||
                trimmed.startsWith("about:")
            ) {
                return trimmed
            }
            if (DOMAIN_REGEX.matches(trimmed)) {
                return "https://$trimmed"
            }
            return "$SEARCH_BASE${Uri.encode(trimmed)}"
        }
    }
}
