package com.colorbrowser.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.colorbrowser.core.TabManager
import com.colorbrowser.core.TabWebView
import com.colorbrowser.ui.components.BouncyIconButton
import com.colorbrowser.ui.components.FloatingNavBar
import com.colorbrowser.ui.components.Radius
import com.colorbrowser.ui.components.glassStrokeColor
import com.colorbrowser.ui.components.glassSurfaceColor
import com.colorbrowser.ui.components.subtleFillColor
import com.colorbrowser.ui.components.themeBackground
import com.colorbrowser.ui.components.themePrimary
import com.colorbrowser.ui.components.themeTextPrimary
import com.colorbrowser.ui.components.themeTextSecondary

private enum class Overlay { None, Tabs, Downloads, Settings }

private enum class NavAction { Back, Forward, Refresh, Home }

/**
 * 浏览器主界面。
 *
 * 布局：WebView 占满 → 顶部细进度条 → 底部悬浮「地址栏 + 导航栏」。
 */
@Composable
fun BrowserScreen(
    nightMode: Int,
    onNightModeChange: (Int) -> Unit,
    initialUrl: String? = null,
    onUrlConsumed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val tabManager = remember { TabManager(context) }

    // 用计数器驱动重组：TabManager 内部是普通字段，非 Compose 状态
    var revision by remember { mutableIntStateOf(0) }
    var overlay by remember { mutableStateOf(Overlay.None) }
    var addressText by remember { mutableStateOf("") }
    var addressFocused by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var isPageLoading by remember { mutableStateOf(false) }

    val webHost = remember { FrameLayout(context) }

    DisposableEffect(tabManager) {
        tabManager.onTabsChanged = { revision++ }
        onDispose {
            tabManager.onTabsChanged = null
            tabManager.destroy()
        }
    }

    LaunchedEffect(Unit) {
        val restored = tabManager.restoreTabs()
        if (!restored) tabManager.newTab()
        revision++
    }

    // 外部链接唤起：等待首次初始化完成后再加载，避免多开标签
    LaunchedEffect(initialUrl) {
        val target = initialUrl ?: return@LaunchedEffect
        val tab = tabManager.ensureTab()
        tab.loadUrl(target)
        onUrlConsumed()
        revision++
    }

    // 依赖 revision 才能拿到最新标签页（TabManager 内部非 Compose 状态）
    val tabs = remember(revision) { tabManager.getTabs() }
    val currentTab: TabWebView? = remember(revision) { tabManager.getCurrentTab() }

    // 地址栏与当前页面同步（用户正在编辑时不打断）
    LaunchedEffect(currentTab?.tabId, currentTab?.url, addressFocused) {
        if (!addressFocused) {
            addressText = currentTab?.url.orEmpty()
        }
    }

    LaunchedEffect(revision) {
        progress = currentTab?.progress ?: 0
        isPageLoading = currentTab?.isLoading == true
    }

    fun submitAddress() {
        val tab = currentTab ?: tabManager.ensureTab()
        val input = addressText.trim()
        if (input.isEmpty()) return
        val normalized = TabWebView.normalizeUrl(input)
        if (normalized.startsWith("http") || normalized.startsWith("file")) {
            tab.loadUrl(normalized)
        } else {
            tab.search(input)
        }
        addressFocused = false
        focusManager.clearFocus()
        revision++
    }

    fun handleNav(action: NavAction) {
        val tab = currentTab ?: return
        when (action) {
            NavAction.Back -> tab.goBack()
            NavAction.Forward -> tab.goForward()
            NavAction.Refresh -> tab.reload()
            NavAction.Home -> tab.loadUrl(tabManager.getHomePage())
        }
        revision++
    }

    BackHandler(enabled = overlay != Overlay.None) { overlay = Overlay.None }
    BackHandler(enabled = overlay == Overlay.None && currentTab?.canGoBack == true) {
        currentTab?.goBack()
        revision++
    }

    Box(modifier = modifier.fillMaxSize().background(themeBackground())) {

        // ===== WebView 宿主 =====
        AndroidView(
            factory = { webHost },
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            update = { container ->
                val tab = currentTab
                if (tab == null) {
                    container.removeAllViews()
                } else {
                    val wv = tab.webView
                    if (wv.parent !== container) {
                        (wv.parent as? ViewGroup)?.removeView(wv)
                        container.removeAllViews()
                        container.addView(
                            wv,
                            FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                    }
                }
            }
        )

        // ===== 顶部加载进度 =====
        AnimatedVisibility(
            visible = isPageLoading,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                progress = (progress.coerceIn(0, 100) / 100f),
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = themePrimary(),
                trackColor = Color.Transparent
            )
        }

        // ===== 底部地址栏 + 悬浮导航 =====
        AnimatedVisibility(
            visible = overlay == Overlay.None,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it / 3 } + fadeIn(),
            exit = slideOutVertically { it / 3 } + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                AddressBar(
                    text = addressText,
                    canGoBack = currentTab?.canGoBack == true,
                    canGoForward = currentTab?.canGoForward == true,
                    onTextChange = { addressText = it },
                    onSubmit = { submitAddress() },
                    onNavigate = { handleNav(it) },
                    onNewTab = {
                        tabManager.newTab()
                        revision++
                    },
                    onFocusChange = { addressFocused = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                FloatingNavBar(
                    selected = 0,
                    onSelected = { index ->
                        when (index) {
                            1 -> overlay = Overlay.Tabs
                            2 -> overlay = Overlay.Downloads
                            3 -> overlay = Overlay.Settings
                            else -> overlay = Overlay.None
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ===== 覆盖层 =====
        when (overlay) {
            Overlay.Tabs -> TabsScreen(
                tabs = tabs,
                currentTabId = currentTab?.tabId ?: -1,
                onSelect = { id ->
                    tabManager.setCurrentTab(id)
                    revision++
                    overlay = Overlay.None
                },
                onClose = { id ->
                    tabManager.closeTab(id)
                    if (tabManager.getTabCount() == 0) tabManager.newTab()
                    revision++
                },
                onNewTab = {
                    tabManager.newTab()
                    revision++
                    overlay = Overlay.None
                },
                onCloseAll = {
                    tabManager.closeAllTabs()
                    tabManager.newTab()
                    revision++
                    overlay = Overlay.None
                },
                onBack = { overlay = Overlay.None }
            )

            Overlay.Downloads -> DownloadsScreen(
                onBack = { overlay = Overlay.None }
            )

            Overlay.Settings -> SettingsScreen(
                tabManager = tabManager,
                nightMode = nightMode,
                onNightModeChange = onNightModeChange,
                onDataCleared = {
                    tabManager.newTab()
                    revision++
                },
                onBack = {
                    revision++
                    overlay = Overlay.None
                }
            )

            Overlay.None -> Unit
        }
    }
}

/**
 * 悬浮地址栏：玻璃质感 + 弹性按钮 + 胶囊输入框
 */
@Composable
private fun AddressBar(
    text: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigate: (NavAction) -> Unit,
    onNewTab: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Radius.xl)
    Surface(
        modifier = modifier.border(1.dp, glassStrokeColor(), shape),
        shape = shape,
        color = glassSurfaceColor(),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            BouncyIconButton(
                icon = Icons.Filled.KeyboardArrowLeft,
                contentDescription = "后退",
                onClick = { onNavigate(NavAction.Back) },
                enabled = canGoBack,
                tint = themeTextPrimary()
            )

            BouncyIconButton(
                icon = Icons.Filled.KeyboardArrowRight,
                contentDescription = "前进",
                onClick = { onNavigate(NavAction.Forward) },
                enabled = canGoForward,
                tint = themeTextPrimary()
            )

            Spacer(Modifier.width(2.dp))

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = RoundedCornerShape(Radius.pill),
                color = subtleFillColor()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { onFocusChange(it.isFocused) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = themeTextPrimary()
                        ),
                        cursorBrush = SolidColor(themePrimary()),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(onGo = { onSubmit() }),
                        decorationBox = { innerTextField ->
                            if (text.isEmpty()) {
                                Text(
                                    text = "搜索或输入网址",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = themeTextSecondary()
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(Modifier.width(2.dp))

            BouncyIconButton(
                icon = Icons.Filled.Refresh,
                contentDescription = "刷新",
                onClick = { onNavigate(NavAction.Refresh) },
                tint = themeTextPrimary()
            )

            BouncyIconButton(
                icon = Icons.Filled.Home,
                contentDescription = "主页",
                onClick = { onNavigate(NavAction.Home) },
                tint = themeTextPrimary()
            )

            BouncyIconButton(
                icon = Icons.Filled.Add,
                contentDescription = "新建标签页",
                onClick = onNewTab,
                tint = themeTextPrimary()
            )
        }
    }
}
