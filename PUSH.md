# 🚀 推送到 GitHub - 快速操作指南

## 方式一：命令行推送（推荐）

```bash
# 进入项目目录
cd ~/Desktop/安卓浏览器/ColorBrowser

# 创建 GitHub 远程仓库后执行：
git remote add origin https://github.com/YOUR_USERNAME/ColorBrowser.git
git push -u origin main
```

## 方式二：GitHub Desktop

1. 打开 [GitHub Desktop](https://desktop.github.com/)
2. File → Add Local Repository → 选择 `ColorBrowser` 目录
3. 点击 Push origin 推送到 GitHub
4. 新建仓库后同步即可

---

## 推送后自动构建 APK

1. 进入 GitHub 仓库 → **Actions** 标签页
2. 点击 **"Build Android APK"** workflow
3. 等待构建完成（约 3-5 分钟）
4. 点击下载产物：**app-debug.apk**
5. 传输到手机安装

---

## 本地构建 APK（如果有 Android Studio）

```bash
./gradlew assembleDebug
# APK 位置: app/build/outputs/apk/debug/app-debug.apk
```
