package com.colorbrowser.core

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.ConcurrentHashMap

/**
 * 多标签页管理。
 *
 * - [tabs] 保存所有 WebView 实例
 * - [tabOrder] 维护展示顺序
 * - 状态持久化到 SharedPreferences，冷启动可恢复
 */
class TabManager(private val context: Context) {

    private val tabs = LinkedHashMap<Int, TabWebView>()
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var currentTabId: Int? = null
    private var nextTabId = 1

    /** UI 刷新回调，由 BrowserScreen 注册 */
    var onTabsChanged: (() -> Unit)? = null

    // ===== 查询 =====

    fun getTabs(): List<TabWebView> = tabs.values.toList()

    fun getTabCount(): Int = tabs.size

    fun getCurrentTab(): TabWebView? = currentTabId?.let { tabs[it] }

    fun getCurrentTabId(): Int? = currentTabId

    // ===== 创建 / 切换 / 关闭 =====

    fun newTab(loadHome: Boolean = true): TabWebView {
        val id = nextTabId++
        val tab = TabWebView(context, id)
        tab.onStateChanged = { onTabsChanged?.invoke() }
        tab.applyPreferences(
            fontSize = getFontSize(),
            desktopMode = isDesktopMode(),
            blockPopups = isBlockPopups(),
            dark = isDarkForKey()
        )
        tabs[id] = tab
        currentTabId = id
        if (loadHome) {
            tab.loadUrl(getHomePage())
        }
        persist()
        onTabsChanged?.invoke()
        return tab
    }

    fun setCurrentTab(tabId: Int) {
        if (tabs.containsKey(tabId)) {
            currentTabId = tabId
            persist()
            onTabsChanged?.invoke()
        }
    }

    fun closeTab(tabId: Int): Boolean {
        val tab = tabs.remove(tabId) ?: return false
        tab.destroy()
        if (currentTabId == tabId) {
            currentTabId = tabs.keys.lastOrNull()
        }
        persist()
        onTabsChanged?.invoke()
        return true
    }

    fun closeAllTabs() {
        tabs.values.forEach { it.destroy() }
        tabs.clear()
        currentTabId = null
        nextTabId = 1
        persist()
        onTabsChanged?.invoke()
    }

    /** 至少保证存在一个标签页 */
    fun ensureTab(): TabWebView {
        getCurrentTab()?.let { return it }
        tabs.values.firstOrNull()?.let {
            currentTabId = it.tabId
            return it
        }
        return newTab()
    }

    // ===== 设置应用 =====

    fun applyGlobalPreferences() {
        val fontSize = getFontSize()
        val desktop = isDesktopMode()
        val blockPopups = isBlockPopups()
        val dark = isDarkForKey()
        tabs.values.forEach { it.applyPreferences(fontSize, desktop, blockPopups, dark) }
        onTabsChanged?.invoke()
    }

    // ===== 持久化 =====

    private fun persist() {
        val editor = prefs.edit()
        val ids = tabs.keys.toList()
        editor.putInt(KEY_TAB_COUNT, ids.size)
        ids.forEachIndexed { index, id ->
            editor.putInt("tab_id_$index", id)
            tabs[id]?.let { tab ->
                editor.putString("tab_url_$id", tab.url)
                editor.putString("tab_title_$id", tab.title)
            }
        }
        currentTabId?.let { editor.putInt(KEY_CURRENT_TAB, it) }
        editor.putInt(KEY_NEXT_ID, nextTabId)
        editor.apply()
    }

    /** 冷启动恢复上次的标签页；无记录时返回 false */
    fun restoreTabs(): Boolean {
        val count = prefs.getInt(KEY_TAB_COUNT, 0)
        if (count <= 0) return false

        val restored = mutableListOf<Int>()
        for (index in 0 until count) {
            val id = prefs.getInt("tab_id_$index", -1)
            if (id < 0) continue
            val savedUrl = prefs.getString("tab_url_$id", null)
            val tab = TabWebView(context, id)
            tab.onStateChanged = { onTabsChanged?.invoke() }
            tab.applyPreferences(
                fontSize = getFontSize(),
                desktopMode = isDesktopMode(),
                blockPopups = isBlockPopups(),
                dark = isDarkForKey()
            )
            if (!savedUrl.isNullOrBlank() && savedUrl != "about:blank") {
                tab.loadUrl(savedUrl)
            }
            tabs[id] = tab
            restored.add(id)
        }

        if (restored.isEmpty()) return false

        val savedCurrent = prefs.getInt(KEY_CURRENT_TAB, -1)
        currentTabId = if (tabs.containsKey(savedCurrent)) savedCurrent else restored.first()
        nextTabId = maxOf(prefs.getInt(KEY_NEXT_ID, 1), (restored.maxOrNull() ?: 0) + 1)
        onTabsChanged?.invoke()
        return true
    }

    // ===== 偏好设置 =====

    fun getHomePage(): String =
        prefs.getString(KEY_HOME_PAGE, DEFAULT_HOME_PAGE) ?: DEFAULT_HOME_PAGE

    fun setHomePage(value: String) {
        val normalized = TabWebView.normalizeUrl(value)
        prefs.edit().putString(KEY_HOME_PAGE, normalized.ifBlank { DEFAULT_HOME_PAGE }).apply()
    }

    fun getFontSize(): Int = prefs.getInt(KEY_FONT_SIZE, 100).coerceIn(50, 250)

    fun setFontSize(value: Int) {
        prefs.edit().putInt(KEY_FONT_SIZE, value.coerceIn(50, 250)).apply()
        applyGlobalPreferences()
    }

    fun isDesktopMode(): Boolean = prefs.getBoolean(KEY_DESKTOP_UA, false)

    fun setDesktopMode(value: Boolean) {
        prefs.edit().putBoolean(KEY_DESKTOP_UA, value).apply()
        applyGlobalPreferences()
    }

    fun isBlockPopups(): Boolean = prefs.getBoolean(KEY_BLOCK_POPUPS, true)

    fun setBlockPopups(value: Boolean) {
        prefs.edit().putBoolean(KEY_BLOCK_POPUPS, value).apply()
        applyGlobalPreferences()
    }

    /** 0 = 跟随系统, 1 = 浅色, 2 = 深色 */
    fun getNightMode(): Int = prefs.getInt(KEY_NIGHT_MODE, 0)

    fun setNightMode(value: Int) {
        prefs.edit().putInt(KEY_NIGHT_MODE, value).apply()
        applyGlobalPreferences()
    }

    private fun isDarkForKey(): Boolean = getNightMode() == 2

    fun clearAllData() {
        closeAllTabs()
        prefs.edit()
            .remove(KEY_HOME_PAGE)
            .remove(KEY_FONT_SIZE)
            .remove(KEY_DESKTOP_UA)
            .remove(KEY_BLOCK_POPUPS)
            .remove(KEY_NIGHT_MODE)
            .apply()
    }

    fun destroy() {
        tabs.values.forEach { it.destroy() }
        tabs.clear()
        currentTabId = null
    }

    companion object {
        private const val PREFS_NAME = "color_browser_prefs"
        private const val DEFAULT_HOME_PAGE = "https://www.baidu.com"

        private const val KEY_TAB_COUNT = "tab_count"
        private const val KEY_CURRENT_TAB = "current_tab_id"
        private const val KEY_NEXT_ID = "next_tab_id"
        private const val KEY_HOME_PAGE = "home_page"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_DESKTOP_UA = "desktop_ua"
        private const val KEY_BLOCK_POPUPS = "block_popups"
        private const val KEY_NIGHT_MODE = "night_mode"
    }
}
