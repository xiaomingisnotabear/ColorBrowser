package com.colorbrowser.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.isSystemInDarkTheme
import com.colorbrowser.core.TabManager
import com.colorbrowser.core.TabWebView
import com.colorbrowser.ui.components.*

/**
 * 浏览器主界面 - 核心入口
 */
@Composable
fun BrowserScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tabManager = remember { TabManager(context) }

    var currentTabIndex by remember { mutableIntStateOf(0) }
    var selectedNav by remember { mutableIntStateOf(0) }
    var showTabs by remember { mutableStateOf(false) }
    var showDownloads by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var addressText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Main) {
            tabManager.restoreTabs()
            val tabs = tabManager.getTabs()
            if (tabs.isNotEmpty()) {
                currentTabIndex = 0
                tabManager.setCurrentTab(tabs[0].tabId)
                addressText = tabs[0].url
                isLoading = tabs[0].isLoading
            } else {
                val tab = tabManager.newTab()
                addressText = tab.url
            }
        }
    }

    fun getCurrentTab(): TabWebView? = tabManager.getTabs().getOrNull(currentTabIndex)

    fun switchToTab(index: Int) {
        val tabs = tabManager.getTabs()
        if (index in tabs.indices) {
            currentTabIndex = index
            tabManager.setCurrentTab(tabs[index].tabId)
            addressText = tabs[index].url
            isLoading = tabs[index].isLoading
            showTabs = false
        }
    }

    fun closeCurrentTab() {
        val tabs = tabManager.getTabs()
        if (tabs.size > 1) {
            tabManager.closeTab(tabs[currentTabIndex].tabId)
            currentTabIndex = (currentTabIndex - 1).coerceAtLeast(0)
            val newTab = tabManager.getTabs().getOrNull(currentTabIndex)
            if (newTab != null) {
                addressText = newTab.url
                isLoading = newTab.isLoading
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = themeBackground(),
        topBar = {
            if (progress > 0 && progress < 100) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(3.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = themePrimary(),
                        trackColor = themePrimary().copy(alpha = 0.2f),
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                factory = { ctx ->
                    val tab = getCurrentTab()
                    val wv = if (tab != null) tab.webView else WebView(ctx).also {
                        it.settings.javaScriptEnabled = true
                        it.settings.domStorageEnabled = true
                    }
                    wv.setBackgroundColor(themeBackground().toInt())
                    wv
                },
                update = { wv ->
                    wv.setWebChromeClient(object : android.webkit.WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress ?: 0
                            isLoading = (newProgress ?: 0) < 100
                        }
                    })
                    wv.setWebViewClient(object : android.webkit.WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            progress = 100
                        }
                        override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                            request?.let {
                                val uri = it.url
                                if (uri.scheme == "http" || uri.scheme == "https") return false
                                try { view?.context?.startActivity(Intent(Intent.ACTION_VIEW, uri)) } catch (e: Exception) {}
                                return true
                            }
                            return false
                        }
                    })
                }
            )

            // 底部导航区域
            AnimatedVisibility(
                visible = !showTabs && !showDownloads && !showSettings,
                enter = slideInVertically { it / 4 } + fadeIn(),
                exit = slideOutVertically { it / 4 } + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 8.dp)
                ) {
                    AddressBar(
                        text = addressText,
                        isLoading = isLoading,
                        canGoBack = (getCurrentTab()?.canGoBack ?: false),
                        canGoForward = (getCurrentTab()?.canGoForward ?: false),
                        onNavigate = { action ->
                            val tab = getCurrentTab() ?: return@AddressBar
                            when (action) {
                                "back" -> tab.goBack()
                                "forward" -> tab.goForward()
                                "refresh" -> tab.reload()
                                "home" -> {
                                    tab.loadUrl(tabManager.getHomePage())
                                    addressText = tabManager.getHomePage()
                                }
                                "new" -> {
                                    val newTab = tabManager.newTab()
                                    currentTabIndex = tabManager.getTabs().size - 1
                                    addressText = newTab.url
                                    showTabs = false
                                }
                            }
                        },
                        onAddressChange = { addressText = it },
                        onSubmit = {
                            val tab = getCurrentTab() ?: run { tabManager.newTab(); currentTabIndex = 0; return@AddressBar }
                            val text = addressText.trim()
                            if (text.startsWith("http://") || text.startsWith("https://")) tab.loadUrl(text) else tab.search(text)
                            isLoading = true
                        },
                        onNewTab = {
                            tabManager.newTab()
                            currentTabIndex = tabManager.getTabs().size - 1
                            addressText = tabManager.getTabs().last().url
                            showTabs = false
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    FloatingNavBar(
                        selected = selectedNav,
                        onSelected = { index ->
                            selectedNav = index
                            when (index) {
                                1 -> showTabs = true
                                2 -> showDownloads = true
                                3 -> showSettings = true
                                else -> { showTabs = false; showDownloads = false; showSettings = false }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // 标签页界面
            AnimatedVisibility(visible = showTabs, enter = slideInVertically { it / 2 } + fadeIn()) {
                TabsScreen(
                    tabs = tabManager.getTabs(),
                    currentIndex = currentTabIndex,
                    onTabClick = { switchToTab(it) },
                    onTabLongPress = { closeCurrentTab() },
                    onNewTab = { tabManager.newTab(); currentTabIndex = tabManager.getTabs().size - 1; showTabs = false },
                    onCloseAll = { tabManager.closeAllTabs(); tabManager.newTab(); currentTabIndex = 0; showTabs = false },
                    onBack = { showTabs = false },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 下载界面
            AnimatedVisibility(visible = showDownloads, enter = slideInVertically { it / 2 } + fadeIn()) {
                DownloadsScreen(onBack = { showDownloads = false; selectedNav = 0 }, modifier = Modifier.fillMaxSize())
            }

            // 设置界面
            AnimatedVisibility(visible = showSettings, enter = slideInVertically { it / 2 } + fadeIn()) {
                SettingsScreen(onBack = { showSettings = false; selectedNav = 0 }, tabManager = tabManager, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun AddressBar(
    text: String,
    isLoading: Boolean,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onNavigate: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNewTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = if (dark) Color(0xE62C2B30) else Color(0xF2FFFFFF),
        shadowElevation = 8.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
            IconButton(onClick = { onNavigate("back") }, enabled = canGoBack, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.ArrowBack, "后退", tint = if (canGoBack) themeTextPrimary() else Color.Gray, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { onNavigate("forward") }, enabled = canGoForward, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.ArrowForward, "前进", tint = if (canGoForward) themeTextPrimary() else Color.Gray, modifier = Modifier.size(18.dp))
            }
            Surface(
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(22.dp),
                color = if (dark) Color(0x1AFFFFFF) else Color(0x0D000000)
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = onAddressChange,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text("搜索或输入网址", style = MaterialTheme.typography.bodyMedium, color = themeTextSecondary())
                        }
                        innerTextField()
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = themeTextPrimary()),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
            }
            val spin by animateFloatAsState(if (isLoading) 360f else 0f, InfiniteRepeatableSpec(tween(1000), RepeatMode.Restart), label = "")
            IconButton(onClick = { onNavigate("refresh") }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Refresh, "刷新", tint = themeTextPrimary(), modifier = Modifier.size(18.dp).rotate(spin))
            }
            IconButton(onClick = { onNavigate("home") }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Home, "主页", tint = themeTextPrimary(), modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onNewTab, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Add, "新标签", tint = themeTextPrimary(), modifier = Modifier.size(18.dp))
            }
        }
    }
}
