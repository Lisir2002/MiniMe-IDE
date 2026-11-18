# ============================================================
# MiniMe-IDE R8 混淆规则
# 设计原则：仅保留「字符串/反射访问」的入口，其余代码真实混淆压缩。
# 本地单测不经过 R8；规则由云端 assembleRelease 校验。
# ============================================================

# --- 调试信息：结构化诊断与崩溃排查依赖行号 ---
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod

# --- WebView Bridge（关键）：JS 以固定对象名 + 方法名反射调用 ---
# @JavascriptInterface 方法名即 JS 契约（window.AndroidBridge.post），不可混淆/移除
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
# 注入对象本身仅由 WebView 反射持有，需整类保留
-keep class com.mini.ide.engine.impl.BridgeEngineImpl$JsBridge { *; }

# --- 枚举：多处通过 valueOf(String) 从 JSON/SharedPreferences 字符串还原 ---
# FileKind / ThemeMode / LogLevel / BridgeMessage / BridgeMethod 等
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Hilt / Dagger 生成的依赖图（反射实例化组件与模块） ---
-keep class dagger.hilt.** { *; }
-keep class **_HiltModules { *; }
-keep class **_HiltModules$* { *; }
-keep class **_HiltComponent { *; }
-keep class **_HiltComponent$* { *; }
-dontwarn dagger.hilt.**

# --- Application：Manifest 引用入口 ---
-keep public class com.mini.ide.MiniMeApp { *; }

# --- Room / Compose / Kotlin 标准库由各自 consumer rules 处理，无需额外 keep ---
