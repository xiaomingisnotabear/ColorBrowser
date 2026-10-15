# 优化
-keepattributes *Annotation*
-keepclassmembers class ** {
    @android.webkit.JavascriptInterface <methods>;
}

# WebView
-dontwarn org.chromium.**
-dontwarn android.net.http.**

# Compose
-dontwarn kotlinx.coroutines.**
-dontwarn androidx.compose.**

# 移除日志
-assumenosideeffect