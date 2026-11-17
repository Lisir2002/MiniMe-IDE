package com.mini.ide.engine.api

/**
 * WebView 能力端口 —— 引擎层与 Android UI 的解耦边界。
 *
 * 设计动机（可控性 + 可测性）：
 * - 引擎铁律要求"不依赖 Compose、不持有 Activity、可独立 JVM 单测"；
 * - 若接口直接吃 `android.webkit.WebView`，纯 JVM 单测无从构造；
 * - 故抽象成纯 Kotlin 接口：真实 WebView 由 :feature:* 层适配注入，测试用 FakeWebViewPort。
 *
 * JS 桥PropertyName 固定为 AndroidBridge，与 BridgeProtocol 约定一致。
 */
interface WebViewPort {
    /** 注入 JS↔Kotlin 桥接对象。 */
    fun addJavascriptInterface(obj: Any, name: String)

    /** 执行 JS 表达式，结果通过 callback 回传（可为 null）。 */
    fun evaluateJavascript(script: String, callback: ((String?) -> Unit)? = null)

    /** 加载 HTML 数据。baseUrl 用于相对路径资源解析。 */
    fun loadDataWithBaseUrl(baseUrl: String?, data: String, mimeType: String, encoding: String, historyUrl: String?)

    /** 重新加载当前页。 */
    fun reload()
}
