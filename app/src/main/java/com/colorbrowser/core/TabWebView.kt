package com.colorbrowser.core

import android.annotation.SuppressLint
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WebView 核心导航逻辑
 * 管理单个标签页的 WebView 实例
 */
class TabWebView(
    val context: Context,
    val tabId: Int
) {
    lateinit var webView: WebView
        private set
    var title: String = "新标签页"
        private set
    var url: String = ""
        private set
    var isLoading: Boolean = false
        private set
    var canGoBack: Boolean = false
        private set
    var canGoForward: Boolean = false
        private set
    var progress: Int = 0
        private set
    var favicon: Bitmap? = null

    private var initialized = false

    init {
        createWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView() {
        webView = WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = false
                displayZoomControls = false
                cacheMode = WebSettings.LOAD_DEFAULT
                if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_WEBVIEW)) {
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progress = newProgress
                    isLoading = newProgress < 100
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    this@TabWebView.title = title ?: "新标签页"
                }

                override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                    favicon = icon
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, u: String?, favicon: Bitmap?) {
                    this@TabWebView.url = u ?: ""
                }

                override fun onPageFinished(view: WebView?, u: String?) {
                    this@TabWebView.url = u ?: ""
                    isLoading = false
                    updateNavigationState()
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    request?.let {
                        val uri = it.url
                        if (uri.scheme == "http" || uri.scheme == "https") return false
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        } catch (e: Exception) {
                            Log.w(TAG, "无法打开链接", e)
                        }
                        return true
                    }
                    return false
                }
            }
        }
    }

    fun loadUrl(url: String) {
        val normalized = normalizeUrl(url)
        if (normalized.isNotEmpty()) webView.loadUrl(normalized)
    }

    fun search(query: String) {
        webView.loadUrl("https://www.google.com/search?q=${Uri.encode(query)}")
    }

    fun goBack(): Boolean {
        if (webView.canGoBack()) {
            webView.goBack()
            return true
        }
        updateNavigationState()
        return false
    }

    fun goForward(): Boolean {
        if (webView.canGoForward()) {
            webView.goForward()
            return true
        }
        updateNavigationState()
        return false
    }

    fun reload() { webView.reload() }
    fun stop() { webView.stopLoading() }

    fun updateNavigationState() {
        canGoBack = webView.canGoBack()
        canGoForward = webView.canGoForward()
    }

    fun getUrl(): String = url

    fun destroy() {
        if (!initialized) return
        initialized = false
        webView.apply {
            stopLoading()
            loadUrl("about:blank")
            webViewClient = null
            webChromeClient = null
            removeAllViews()
            destroy()
        }
    }

    companion object {
        private const val TAG = "TabWebView"

        fun normalizeUrl(input: String): String {
            val trimmed = input.trim()
            if (trimmed.isEmpty()) return ""
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
            val domainPattern = "^(?!(?:\\d+\\.)+)(?:[a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}$"
            if (trimmed.matches(Regex(domainPattern))) return "https://$trimmed"
            return "https://www.google.com/search?q=${Uri.encode(trimmed)}"
        }
    }
}
