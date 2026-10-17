# ColorBrowser 项目概述 & 后续工作指南

> 本文档供接手 AI/开发者阅读，完整了解项目现状、技术决策和待办事项。

---

## 一、项目定位

**ColorBrowser** — 基于 ColorOS 17 流体设计语言的轻量级 Android 浏览器

- **对标产品**：Via 浏览器（极致精简）
- **差异化**：ColorOS 流体美学（液态亚克力、强圆角、弹性反馈）
- **技术栈**：Kotlin + Jetpack Compose + 系统 WebView
- **最低兼容**：Android 10 (API 26)
- **目标 API**：Android 14 (API 34)

---

## 二、已完成的架构

```
ColorBrowser/
├── .github/workflows/build.yml        # CI：Android Studio build + 上传 APK artifact
├── gradlew                            # Gradle Wrapper 启动脚本（已修复引号格式）
├── gradle/wrapper/
│   ├── gradle-wrapper.properties      # Gradle 8.6
│   └── gradle-wrapper.jar             # 从 GitHub API 下载的合法 JAR（43KB）
├── app/
│   ├── build.gradle.kts               # 模块配置：Kotlin 1.9.24 + Compose BOM 2024.01.00
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/colorbrowser/
│       │   ├── ColorBrowserApp.kt     # Application 入口（占位，功能简单）
│       │   ├── core/
│       │   │   ├── TabWebView.kt      # 单标签页 WebView 封装（导航/URL规范化/下载拦截）
│       │   │   └── TabManager.kt      # 多标签页生命周期管理（创建/关闭/持久化）
│       │   └── ui/
│       │       ├── MainActivity.kt    # Compose 入口 + Light/Dark 配色
│       │       ├── components/
│       │       │   ├── DesignTokens.kt    # 半径/间距/动画常量
│       │       │   ├── FluidComponents.kt # GlassSurface / FloatingNavBar 组件
│       │       │   └── ThemeColors.kt   # themePrimary()/themeBackground() 等 Composable
│       │       └── screens/
│       │           ├── BrowserScreen.kt   # 主界面：WebView + 地址栏 + 导航栏（核心文件）
│       │           ├── TabsScreen.kt      # 标签页管理（列表+新建）
│       │           ├── DownloadsScreen.kt # 下载管理（模拟数据，待接入真实 DownloadManager）
│       │           └── SettingsScreen.kt  # 设置（夜间模式/字体/用户代理/广告拦截）
│       └── res/
│           ├── drawable/              # 玻璃背景、图标
│           ├── values/                # 颜色、字符串、主题（浅色）
│           ├── values-night/          # 深色主题
│           └── xml/                   # FileProvider、备份规则
├── build.gradle.kts                   # 根构建：仅 com.android.application + kotlin.android 两个插件
└── settings.gradle.kts                # 模块声明
```

---

## 三、关键技术决策 & 已知问题

### 已解决的历史问题

| 问题 | 修复方式 |
|------|---------|
| `gradle-wrapper.jar` 损坏（原始为无效文件） | 通过 GitHub API 重新下载合法 JAR（blob SHA: d64cd4917707c1f8861d8cb53dd15194d4248596） |
| `gradlew` 脚本引号格式错误导致 `-Xms64m: not found` | 改为 `DEFAULT_JVM_OPTS=-Xmx64m -Xms64m`（无引号） |
| 根 `build.gradle.kts` plugins 块语法错误 | 改为 `id("...") version "..." apply false` 标准 Kotlin DSL 写法 |
| `org.jetbrains.kotlin.plugin.compose` 插件不存在 | 移除，改用 `buildFeatures { compose = true }` + `kotlinCompilerExtensionVersion` |
| 网络不稳定导致多次推送失败 | 已通过 CI 多次重试最终成功推送 |

### 当前构建状态

- **最新 commit**：`7c113b7`
- **GitHub Actions Run**：[39031390509](https://github.com/xiaomingisnotabear/ColorBrowser/actions/runs/39031390509)
- **APK 产出路径**（成功后）：`app/build/outputs/apk/debug/app-debug.apk`
- **Actions workflow**：[build.yml](https://github.com/xiaomingisnotabear/ColorBrowser/blob/main/.github/workflows/build.yml)

### 已知未解决/待验证的问题

1. **`FluidComponents.kt` 中 `PressIndicatorModifier` 是占位实现** — 实际触摸反馈依赖 Material3 内置机制，自定义 Pressable 逻辑不完整，需要清理或替换
2. **`BrowserScreen.kt` 中 `AndroidView` 的 `update` 回调调用 `wv.scrollVertically(0, 0)` 是错误的** — 实际进度/状态由 `WebChromeClient` 和 `WebViewClient` 回调驱动，应删除这行
3. **`DownloadListener` 在 `TabWebView` 中只打印日志，未真正实现下载** — 需接入 `DownloadManager` API
4. **`SettingsScreen.kt` 中字体大小变更不会实时应用到 WebView** — 需要在 `TabWebView.loadUrl()` 或新建标签时读取并设置 `textZoom`
5. **广告拦截（`block_ads`）当前只是 UI toggle，无实际拦截逻辑** — 需要注入 JavaScript 或使用 `shouldInterceptRequest` 过滤广告域名

---

## 四、待完成的工作（按优先级排序）

### P0 — 修复编译/运行时 bug（必须先做）

- [ ] **修复 `BrowserScreen.kt`**：删除 `AndroidView.update` 中无效的 `scrollVertically` 调用
- [ ] **修复 `FluidComponents.kt`**：移除占位 `PressIndicatorModifier`，用标准 `Modifier.clickable` + scale 动画替代
- [ ] **验证 Compose 版本兼容性**：`compose-bom:2024.01.00` + `kotlinCompilerExtensionVersion = "1.5.14"` 是否与 Kotlin 1.9.24 兼容；如不兼容需升级到 BOM 2024.02.00+
- [ ] **运行完整构建**：确认 CI 能产出 APK，修复所有编译错误后再进行后续开发

### P1 — 核心功能补全

- [ ] **接入真实下载管理器**：使用 `android.app.DownloadManager` 处理 `onDownloadStart` 事件，在 `DownloadsScreen` 中显示真实下载列表
- [ ] **WebView 字体缩放**：在 `TabWebView` 加载页面时读取 `tabManager.getFontSize()` 并设置 `webView.settings.textZoom`
- [ ] **地址栏历史搜索记录**：使用 `SharedPreferences` 保存最近搜索词，点击地址栏时下拉展示
- [ ] **首页快捷图标**：在 `BrowserScreen` 空白页展示常用网站快捷入口（类似 Via 主页）

### P2 — ColorOS 流体设计完善

- [ ] **滚动形变动画**：在 `FloatingNavBar` 中添加基于 `NestedScrollConnection` 的轻微缩放效果
- [ ] **液态亚克力效果增强**：在 `GlassSurface` 中添加顶部高光描边（1dp 白色半透明）
- [ ] **页面加载过渡动画**：WebView 切换页面时添加淡入淡出效果（`AlphaAnimation` 或 Compose `crossfade`）
- [ ] **弹性回弹**：地址栏输入框获得焦点时的水波纹效果

### P3 — 增强功能

- [ ] **广告拦截**：通过 `shouldInterceptRequest` 拦截已知广告域名，或使用内置的简易规则集
- [ ] **阅读模式**：长按网页提取正文内容，去除广告和侧边栏
- [ ] **书签功能**：持久化书签列表（SQLite 或 DataStore），可在侧边栏快速访问
- [ ] **手势操作**：左右滑动切换标签页，下拉刷新
- [ ] **多语言支持**：当前 `strings.xml` 只有中文，添加英文资源

### P4 — 性能优化 & 发布准备

- [ ] **ProGuard 规则细化**：当前 `proguard-rules.pro` 过于简略，需要补充 WebView/Compose 相关 keep 规则
- [ ] **APK 体积分析**：使用 Android Studio Built APK Analyzer 确认各依赖体积，移除不必要的库
- [ ] **签名配置**：生成测试密钥对，配置 `keystore.properties`（注意不要提交到 Git）
- [ ] **版本迭代**：当前 versionCode=1，设计版本号策略

---

## 五、环境变量 & 敏感信息

- **GitHub Token**：已包含在历史推送记录中（`ghp_SIEX9A...`），建议轮换旧 token
- **无其他敏感配置**：项目不涉及第三方 API Key

---

## 六、构建命令

```bash
# 本地构建（需要 Android Studio + JDK 17 + SDK 34）
cd ~/Desktop/安卓浏览器/ColorBrowser
./gradlew assembleDebug
# APK 位置：app/build/outputs/apk/debug/app-debug.apk

# 云端构建（GitHub Actions）
# 推送任意 commit 到 main 分支自动触发
# 或在 GitHub 仓库 → Actions → Build Android APK → Run workflow
```

---

*文档生成时间：2025-10-17 | 项目地址：https://github.com/xiaomingisnotabear/ColorBrowser*
