package com.colorbrowser.core

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 标签页管理器
 * 管理多个标签页的生命周期
 */
class TabManager(private val context: Context) {
    private val tabs = ConcurrentHashMap<Int, TabWebView>()
    private val tabOrder = mutableListOf<Int>()
    private var currentTabId: Int? = null
    private var nextTabId = 1

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tabs", Context.MODE_PRIVATE)

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * 获取或创建标签页
     */
    fun getOrCreateTab(tabId: Int): TabWebView {
        return tabs.getOrPut(tabId) {
            TabWebView(context, tabId)
        }
    }

    /**
     * 创建新标签页
     */
    fun newTab(): TabWebView {
        val id = nextTabId++
        val tab = getOrCreateTab(id)
        tabOrder.add(id)
        setCurrentTab(id)

        // 加载主页或空白页
        val homePage = prefs.getString("homepage", "https://www.google.com")
        tab.loadUrl(homePage ?: "about:blank")

        persistState()
        return tab
    }

    /**
     * 设置当前标签页
     */
    fun setCurrentTab(tabId: Int) {
        currentTabId = tabId
    }

    /**
     * 获取当前标签页
     */
    fun getCurrentTab(): TabWebView? {
        return currentTabId?.let { tabs[it] }
    }

    /**
     * 关闭标签页
     */
    fun closeTab(tabId: Int): Boolean {
        val tab = tabs.remove(tabId) ?: return false
        tabOrder.remove(tabId)
        tab.destroy()

        // 如果关闭的是当前标签，切换到最后一个
        if (currentTabId == tabId) {
            currentTabId = tabOrder.lastOrNull()
        }

        persistState()
        return true
    }

    /**
     * 关闭所有标签页
     */
    fun closeAllTabs() {
        tabs.values.forEach { it.destroy() }
        tabs.clear()
        tabOrder.clear()
        currentTabId = null
        nextTabId = 1
        persistState()
    }

    /**
     * 获取所有标签页（按顺序）
     */
    fun getTabs(): List<TabWebView> {
        return tabOrder.mapNotNull { tabs[it] }
    }

    /**
     * 获取标签页数量
     */
    fun getTabCount(): Int = tabOrder.size

    /**
     * 保存标签页状态到持久化存储
     */
    private fun persistState() {
        val editor = prefs.edit()
        tabOrder.forEach { tabId ->
            val tab = tabs[tabId]
            if (tab != null) {
                editor.putString("tab_$tabId", tab.url)
                editor.putString("tab_${tabId}_title", tab.title)
            }
        }
        tabOrder.forEachIndexed { index, tabId ->
            editor.putInt("tab_order_$index", tabId)
        }
        editor.putInt("tab_order_count", tabOrder.size)
        if (currentTabId != null) {
            editor.putInt("current_tab", currentTabId!!)
        }
        editor.putInt("next_tab_id", nextTabId)
        editor.apply()
    }

    /**
     * 恢复标签页状态
     */
    fun restoreTabs() {
        // 先恢复 order
        val count = prefs.getInt("tab_order_count", 0)
        for (i in 0 until count) {
            val tabId = prefs.getInt("tab_order_$i", -1)
            if (tabId >= 0) {
                val url = prefs.getString("tab_$tabId", null)
                if (url != null && url != "about:blank") {
                    val tab = getOrCreateTab(tabId)
                    if (tabId !in tabOrder) {
                        tabOrder.add(tabId)
                    }
                    tab.loadUrl(url)
                }
            }
        }

        // 恢复当前标签
        val currentId = prefs.getInt("current_tab", -1)
        if (currentId >= 0 && tabs.containsKey(currentId)) {
            setCurrentTab(currentId)
        } else {
            currentTabId = tabOrder.firstOrNull()
        }

        // 恢复 nextTabId
        nextTabId = prefs.getInt("next_tab_id", 1)
    }

    /**
     * 获取主页设置
     */
    fun getHomePage(): String = prefs.getString("homepage", "https://www.google.com") ?: ""

    /**
     * 设置主页
     */
    fun setHomePage(url: String) {
        prefs.edit().putString("homepage", url).apply()
    }

    /**
     * 设置字体大小
     */
    fun setFontSize(size: Int) {
        prefs.edit().putInt("font_size", size).apply()
    }

    /**
     * 获取字体大小
     */
    fun getFontSize(): Int = prefs.getInt("font_size", 100)

    /**
     * 获取偏好设置
     */
    fun getBooleanPreference(key: String, default: Boolean): Boolean =
        prefs.getBoolean(key, default)

    /**
     * 设置偏好设置
     */
    fun setBooleanPreference(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    /**
     * 获取字符串偏好
     */
    fun getStringPreference(key: String, default: String): String =
        prefs.getString(key, default) ?: default

    /**
     * 设置字符串偏好
     */
    fun setStringPreference(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    /**
     * 清除所有缓存
     */
    fun clearCache() {
        prefs.edit().clear().apply()
        tabs.values.forEach { it.destroy() }
        tabs.clear()
        tabOrder.clear()
        currentTabId = null
        nextTabId = 1
    }

    /**
     * 释放资源
     */
    fun destroy() {
        scope.cancel()
        closeAllTabs()
    }
}

/**
 * 偏好设置 Key
 */
object Prefs {
    const val NIGHT_MODE = "night_mode"
    const val BLOCK_POPUPS = "block_popups"
    const val BLOCK_ADS = "block_ads"
    const val FONT_SIZE = "font_size"
    const val USER_AGENT_MOBILE = "user_agent_mobile"
    const val HOMEPAGE = "homepage"
}
