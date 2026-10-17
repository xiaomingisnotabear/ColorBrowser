# 保留 WebView JS 接口
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# WebView
-dontwarn android.webkit.**

# Kotlin 协程
-dontwarn kotlinx.coroutines.**

# 保留行号便于排查崩溃
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
