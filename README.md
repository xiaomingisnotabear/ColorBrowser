# Color 浏览器 (ColorBrowser)

基于 **Kotlin + Jetpack Compose + 系统 WebView** 的轻量 Android 浏览器，
UI 采用 ColorOS 流体设计语言：半透明玻璃层、强圆角、悬浮底栏、弹性反馈。

| | |
|---|---|
| Release APK | **1.3 MB** |
| 最低版本 | Android 8.0 (API 26) |
| 目标版本 | Android 14 (API 34) |
| 渲染 | 系统 WebView（不打包内核） |

## 功能

- 地址栏智能解析：完整 URL / 域名补全 / 关键词走搜索
- 前进 · 后退 · 刷新 · 主页 · 新建标签
- 多标签页：切换 / 单独关闭 / 全部关闭 / 冷启动恢复
- 真实下载：拦截页面下载 → 系统 `DownloadManager` → 列表查询与打开
- 夜间模式三态（跟随系统 / 浅色 / 深色）
- 字体缩放 50%–250%、桌面版 UA、阻止弹窗
- 自定义主页、清除浏览数据
- 外部链接唤起（`ACTION_VIEW`）

## 构建

需要 JDK 17 与 Android SDK 34。

```bash
export JAVA_HOME=/path/to/jdk17
export ANDROID_HOME=/path/to/android-sdk

./gradlew assembleRelease
# 产物：app/build/outputs/apk/release/app-release.apk
```

签名使用项目根目录的 `colorbrowser.keystore`（口令均为 `colorbrowser`，见 `app/build.gradle.kts`）。
该文件不入库，CI 会临时生成。

## 安装

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

或把 APK 传到手机后点击安装（需允许「未知来源」）。

## 结构

```
app/src/main/java/com/colorbrowser/
├── core/        TabWebView · TabManager · Downloads
├── ui/components/  DesignTokens · ThemeColors · FluidComponents
└── ui/screens/  BrowserScreen · TabsScreen · DownloadsScreen · SettingsScreen
```

详细技术说明与交接事项见 [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md)。

## 许可证

MIT
