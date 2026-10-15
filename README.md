# ColorBrowser - 轻量级 ColorOS 风格 Android 浏览器

基于 **Kotlin + Jetpack Compose + WebView** 的极简浏览器，UI 参考 ColorOS 17 "流体设计"语言：

- 液态亚克力玻璃效果
- 强圆角、半透明层叠
- 悬浮底部导航
- 滚动时轻微形变
- 弹性反馈

## 核心特性

| 功能 | 说明 |
|------|------|
| 网页浏览 | 直接调用系统 WebView 渲染 |
| 标签页管理 | 多标签页切换、新建、关闭 |
| 地址栏 | 搜索/输入网址，智能识别 |
| 导航控制 | 前进/后退/刷新/主页 |
| 下载管理 | 网页内资源下载 |
| 夜间模式 | 自动/手动切换深色主题 |
| 字体调节 | 50% - 200% 缩放 |
| 广告拦截 | 可选开启 |

## 技术栈

- **语言**: Kotlin 1.9+
- **UI**: Jetpack Compose (Material 3)
- **渲染**: Android WebView (系统)
- **兼容**: Android 10 (API 26) 及以上
- **目标**: Android 14 (API 34)

## 构建

### 前置条件

- Android Studio Hedgehog 或更高
- JDK 17
- Android SDK 34+

### 命令行构建

```bash
cd ColorBrowser
./gradlew assembleDebug
```

APK 输出位置：`app/build/outputs/apk/debug/app-debug.apk`

### Release 构建（R8 混淆 + 资源压缩）

```bash
./gradlew assembleRelease
```

### APK 大小优化

已在 `build.gradle.kts` 中配置：

```kotlin
buildTypes {
    release {
        minifyEnabled true          // R8 代码混淆
        shrinkResources true        // 移除未使用资源
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

**预期 Release APK 大小**：约 **1.5 - 2.5 MB**（不含 WebView 引擎，依赖系统 WebView）

## 项目结构

```
app/src/main/java/com/colorbrowser/
├── ColorBrowserApp.kt              # Application 入口
├── core/
│   ├── TabManager.kt              # 标签页生命周期管理
│   └── TabWebView.kt              # 单个标签页的 WebView 封装
├── ui/
│   ├── MainActivity.kt            # 主 Activity + Compose 入口
│   ├── components/
│   │   ├── DesignTokens.kt        # 设计令牌（颜色/圆角/间距/动画）
│   │   └── FluidComponents.kt     # 流体设计组件（玻璃表面/弹性按钮/导航栏）
│   └── screens/
│       ├── BrowserScreen.kt       # 浏览器主界面
│       ├── TabsScreen.kt          # 标签页管理界面
│       ├── DownloadsScreen.kt     # 下载管理界面
│       └── SettingsScreen.kt     # 设置界面
app/src/main/res/
├── drawable/glass_background.xml  # 玻璃效果背景
├── values/
│   ├── colors.xml                 # 配色方案（浅色/深色）
│   ├── strings.xml               # 字符串资源
│   └── themes.xml                # 主题（透明状态栏）
├── values-night/
│   └── themes.xml                # 深色主题
└── xml/
    ├── backup_rules.xml
    ├── data_extraction_rules.xml
    └── file_paths.xml
```

## ColorOS 流体设计实现

### 液态亚克力

```kotlin
// 半透明表面 + 描边 + 阴影
GlassSurface(cornerRadius = 24.dp) {
    // 内容
}
```

- 浅色模式：`#CCFFFFFF` (80% 白) + 白色描边
- 深色模式：`#992C2B30` (60% 深灰) + 白色描边
- 顶部 1px 高光模拟玻璃反射

### 强圆角

```kotlin
object Radius {
    val XS = 8.dp
    val SM = 12.dp
    val MD = 16.dp
    val LG = 24.dp    // 主卡片圆角
    val XL = 32.dp    // 大卡片圆角
    val PILL = 999.dp // 胶囊形
}
```

### 弹性反馈

```kotlin
// 低阻尼弹簧
val FluidSpring = SpringSpec(
    dampingRatio = Spring.DampingRatioLow,   // 轻微振荡
    stiffness = Spring.StiffnessMediumLow
)

// 按钮按压缩放 0.95 → 回弹 1.0
```

### 悬浮底部导航

```kotlin
FloatingNavBar(selected = 0, onSelected = { ... })
// 独立圆角玻璃卡片，不与底部系统栏融合
```

## 权限

```xml
INTERNET
ACCESS_NETWORK_STATE
ACCESS_WIFI_STATE
WRITE_EXTERNAL_STORAGE (SDK < 32)
```

## 基于开源项目的参考

本项目参考了以下开源项目的实现思路：

- [Via 浏览器](https://github.com/via-browser/via) - 极简浏览器架构
- [Firefox Focus](https://github.com/mozilla-mobile/focus) - 隐私优先设计
- [Ktor/OkHttp 下载拦截](https://github.com/square/okhttp) - 下载处理
- [Android WebView](https://developer.android.com/reference/android/webkit/WebView) - 渲染

## 许可证

MIT License
