# ColorBrowser 项目概述 & 交接指南

> 最后一次更新：2026-10-18 · 状态：**编译通过，已产出可安装 APK**

---

## 一、项目定位

**ColorBrowser（Color 浏览器）** — 基于 ColorOS 17 流体设计语言的轻量级 Android 浏览器

| 项 | 值 |
|---|---|
| 对标 | Via 浏览器 |
| 差异化 | ColorOS 流体美学（半透明玻璃层、强圆角、弹性反馈） |
| 技术栈 | Kotlin 1.9.22 + Jetpack Compose (BOM 2024.02.00) + 系统 WebView |
| minSdk | 26（Android 8.0，覆盖 Android 10+ 要求） |
| targetSdk / compileSdk | 34 |
| Release APK 体积 | **1.3 MB** |
| Debug APK 体积 | 15 MB（含 tooling 与未混淆的 icons-extended） |

---

## 二、当前构建状态（已验证）

本地已完成真实构建（JDK 17 + Android SDK 34）：

```bash
./gradlew assembleDebug    # ✅ BUILD SUCCESSFUL
./gradlew assembleRelease  # ✅ BUILD SUCCESSFUL
```

产物：
- `app/build/outputs/apk/release/app-release.apk` — **已签名，可直接安装（1.3MB）**
- `app/build/outputs/apk/debug/app-debug.apk` — 调试版（debug 签名）

签名信息：
- keystore：项目根目录 `colorbrowser.keystore`（storepass / keypass / alias 均为 `colorbrowser`）
- **该 keystore 已被 .gitignore 排除，不进版本库**；CI 会临时生成一份

验证结果：
- `apksigner verify` → 签名有效
- `aapt dump badging` → package `com.colorbrowser`，minSdk 26，targetSdk 34，launcher activity 正常

---

## 三、本地编译环境（本机已装好，但不在系统 PATH）

这台 Mac 原本没有 JDK / Android SDK，已手动装到用户目录：

```bash
export JAVA_HOME="$HOME/devtools/jdk-17.0.20.1+1/Contents/Home"
export ANDROID_HOME="$HOME/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
```

- JDK：`~/devtools/jdk-17.0.20.1+1/Contents/Home`（Temurin 17）
- SDK：`~/Android/sdk`（platforms;android-34、build-tools;34.0.0、platform-tools）
- `local.properties` 已写入 `sdk.dir`（该文件被 gitignore）

> 注意：`dl.google.com` 下载速度约 180KB/s，SDK 组件下载较慢属正常现象。

---

## 四、目录结构

```
ColorBrowser/
├── .github/workflows/build.yml        # CI：生成临时 keystore → 构建 debug + release → 上传两个 APK
├── gradlew                            # 自定义启动脚本（见「踩坑记录」）
├── colorbrowser.keystore              # 本地签名密钥（gitignore）
├── local.properties                   # sdk.dir（gitignore）
├── app/src/main/java/com/colorbrowser/
│   ├── ColorBrowserApp.kt             # Application 入口
│   ├── core/
│   │   ├── TabWebView.kt              # 单标签 WebView 封装（导航/标题/图标/下载/UA/字号/深色底）
│   │   ├── TabManager.kt              # 多标签管理 + 设置持久化 + 状态恢复
│   │   └── Downloads.kt               # DownloadEntry + DownloadRepository（真机 DownloadManager）
│   └── ui/
│       ├── MainActivity.kt            # 主题宿主、夜间模式三态、系统栏配色、外部链接唤起
│       ├── components/
│       │   ├── DesignTokens.kt        # Radius / Spacing / FluidSpring
│       │   ├── ThemeColors.kt         # LocalAppDark + 语义色 + Light/Dark ColorScheme
│       │   └── FluidComponents.kt     # GlassSurface / BouncyIconButton / TabCard / FloatingNavBar
│       └── screens/
│           ├── BrowserScreen.kt       # 主界面（WebView 宿主 + 地址栏 + 悬浮导航 + 覆盖层）
│           ├── TabsScreen.kt          # 标签页卡片列表
│           ├── DownloadsScreen.kt     # 真实下载列表（2 秒轮询）
│           └── SettingsScreen.kt      # 外观/阅读/浏览/主页/数据/关于
└── app/src/main/res/                  # 主题用平台 Material 主题（不依赖 com.google.android.material）
```

---

## 五、已实现功能

- ✅ 网页渲染（系统 WebView，JS / DOM Storage / Cookie / 混合内容）
- ✅ 地址栏智能解析（协议 / 域名 / 关键词 → 百度搜索）
- ✅ 前进 / 后退 / 刷新 / 主页 / 新建标签
- ✅ 多标签页（新建、切换、关闭单个、关闭全部、冷启动恢复）
- ✅ 真实下载（拦截 `onDownloadStart` → 系统 DownloadManager → 列表查询 / 打开 / 删除）
- ✅ 夜间模式三态（跟随系统 / 浅色 / 深色），CompositionLocal 全局覆盖
- ✅ 字体缩放 50%–250%（实时应用到所有标签）
- ✅ 桌面版 UA 切换
- ✅ 阻止弹窗（`javaScriptCanOpenWindowsAutomatically`）
- ✅ 主页自定义、清除浏览数据
- ✅ 外部链接唤起（`ACTION_VIEW` http/https，含 `singleTop` 的 onNewIntent）
- ✅ 返回键：先退覆盖层，再退网页历史

---

## 六、架构要点（接手必读）

### 1. 状态刷新机制（重要）
`TabManager` / `TabWebView` 内部字段是**普通 Kotlin 字段**，不是 Compose 状态。
UI 刷新依赖 `BrowserScreen` 里的 `revision: Int` 计数器：

```kotlin
DisposableEffect(tabManager) {
    tabManager.onTabsChanged = { revision++ }
    onDispose { tabManager.onTabsChanged = null; tabManager.destroy() }
}
val tabs = remember(revision) { tabManager.getTabs() }
val currentTab = remember(revision) { tabManager.getCurrentTab() }
```

**任何改变标签状态的操作后都要 `revision++`**，否则界面不刷新。

### 2. WebView 附着策略
不做「每个标签一个 AndroidView」（会因 parent 冲突崩溃），而是用**单个 remembered FrameLayout 作为宿主**，
在 `AndroidView.update` 里把当前标签的 WebView 换进去：

```kotlin
val webHost = remember { FrameLayout(context) }
AndroidView(factory = { webHost }, update = { container ->
    if (webView.parent !== container) {
        (webView.parent as? ViewGroup)?.removeView(webView)
        container.removeAllViews()
        container.addView(webView, MATCH_PARENT, MATCH_PARENT)
    }
})
```

### 3. 深色模式
`MainActivity` 用 `CompositionLocalProvider(LocalAppDark provides dark)` 全局下发，
所有语义色函数（`themePrimary()` 等）读 `LocalAppDark.current`，因此**手动切换能覆盖系统**。
不要再用 `isSystemInDarkTheme()` 直接取色。

### 4. GlassSurface 不能强制 fillMaxSize
早期版本 `Box(Modifier.fillMaxSize())` 在 LazyColumn 中会因无限高度约束崩溃。
当前实现用 `Surface { Column { content() } }`，**天然 wrapContent**，可安全放入列表。

---

## 七、踩坑记录（避免重复）

| 问题 | 原因 | 修复 |
|---|---|---|
| 根 `build.gradle.kts` 报 `Too many characters in a character literal` | 混入 Groovy 写法 `id('x') version '1' apply false` | 改为 `id("x") version "1" apply false` |
| `kotlin.plugin.compose` 插件找不到 | 该插件 artifact 属 Kotlin 2.x，Kotlin 1.9 无此坐标 | 移除，改用 `buildFeatures.compose` + `kotlinCompilerExtensionVersion` |
| Compose Compiler 版本不匹配 | 1.5.8 只配 Kotlin 1.9.22 | 根 build 固定 Kotlin **1.9.22** |
| `./gradlew: 6: -Xms64m: not found` | 手写 gradlew 里 `DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'` 引号被当成类名 | 去掉引号：`-Xmx64m -Xms64m` |
| `gradle-wrapper.jar` 无效 | 早期下载被网络劫持成 HTML | 通过 GitHub Blob API 取真实 JAR（43462 字节） |
| `Theme.MaterialComponents.*` 链接失败 | 未依赖 `com.google.android.material` | 父主题换成 `android:Theme.Material.Light.NoActionBar`（夜间用 `android:Theme.Material.NoActionBar`） |
| `?android:attr/isLightTheme` 报错 | 私有属性 | 直接写 `true` / `false` |
| 重复声明 `Radius`/`Spacing`/`themePrimary` 等 | DesignTokens/ThemeColors/FluidComponents 三处重复 | 已合并，单一来源 |
| `LocalDensity.current` 在 `object` 里调用 | 组合上下文外调用 @Composable | 直接用 `8.dp` 字面量 |
| `DownloadManager.Query.setSortingOrder` 不存在 | 该 API 无排序 | 查询后在 Kotlin 侧 `sortedByDescending { it.id }` |

---

## 八、已知限制 / 待办

### P0（建议优先）
- [ ] **真机冒烟测试**：尚未在设备上跑过（本机无连接设备、未装模拟器）。首次安装后重点验证：启动不崩、地址栏输入、标签切换、下载、夜间切换。
- [ ] 输入法弹出时底部地址栏可能被顶起遮挡，建议给底部容器加 `Modifier.imePadding()`

### P1（功能补全）
- [ ] 搜索历史 / 地址栏自动补全
- [ ] 首页快捷图标（九宫格导航），替代默认跳百度
- [ ] 书签（DataStore 或 SQLite）
- [ ] 长按链接 / 图片的上下文菜单
- [ ] 下载列表的「打开所在文件夹」

### P2（设计打磨）
- [ ] 滚动时导航栏轻微形变（`NestedScrollConnection` + scale）
- [ ] 页面切换淡入淡出
- [ ] 标签切换的共享元素过渡
- [ ] 地址栏聚焦时的玻璃高光增强

### P3（工程化）
- [ ] 广告拦截（`shouldInterceptRequest` 域名规则或 hosts 方案）
- [ ] 阅读模式
- [ ] 手势（左右滑切标签、下拉刷新）
- [ ] 英文 `values-en` 资源
- [ ] 把 `material-icons-extended` 换成少量自绘 vector，进一步压包体

---

## 九、构建命令

```bash
cd ~/Desktop/安卓浏览器/ColorBrowser

export JAVA_HOME="$HOME/devtools/jdk-17.0.20.1+1/Contents/Home"
export ANDROID_HOME="$HOME/Android/sdk"

./gradlew assembleDebug     # 调试包
./gradlew assembleRelease   # 签名发布包（1.3MB）

# 安装到已连接设备
$ANDROID_HOME/platform-tools/adb install -r app/build/outputs/apk/release/app-release.apk
```

CI：推送到 `main` 即自动构建，两个 APK 作为 artifact 上传。

---

## 十、安全提示

- `colorbrowser.keystore` 口令是明文硬编码在 `app/build.gradle.kts` 里的（`colorbrowser`）。
  这是**个人自用**的便利做法；若要上架应用商店，请改为读取 `keystore.properties` 并不入库。
- 历史上曾把 GitHub PAT 直接写在命令里，**建议到 GitHub → Settings → Tokens 轮换/删除旧 token**。
