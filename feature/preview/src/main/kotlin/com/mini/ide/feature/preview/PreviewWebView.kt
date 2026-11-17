package com.mini.ide.feature.preview

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.mini.ide.engine.api.WebViewPort
import org.json.JSONObject

/** 预览 WebView 的 WebViewPort 适配。 */
class PreviewWebViewPort(val webView: WebView) : WebViewPort {
    override fun addJavascriptInterface(obj: Any, name: String) = webView.addJavascriptInterface(obj, name)
    override fun evaluateJavascript(script: String, callback: ((String?) -> Unit)?) =
        webView.evaluateJavascript(script, callback)
    override fun loadDataWithBaseUrl(baseUrl: String?, data: String, mimeType: String, encoding: String, historyUrl: String?) =
        webView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    override fun reload() = webView.reload()
}

/**
 * 预览专用 JS↔Kotlin 桥（决策 3）。注入名同样是 AndroidBridge。
 * 解析 JS 回传的 {type,payload} 并交给 [onMessage]。
 */
class PreviewJsBridge(private val onMessage: (String, String) -> Unit) {
    @JavascriptInterface
    fun post(json: String) {
        runCatching {
            val obj = JSONObject(json)
            val type = obj.optString("type", "")
            val payload = obj.optString("payload", "")
            onMessage(type, payload)
        }
    }
}

/** 预览 WebView Composable。 */
@Composable
fun PreviewWebView(
    onPortReady: (PreviewWebViewPort) -> Unit,
    onMessage: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context: Context ->
            createPreviewWebView(context).also { webView ->
                webView.addJavascriptInterface(PreviewJsBridge(onMessage), "AndroidBridge")
                onPortReady(PreviewWebViewPort(webView))
            }
        },
        modifier = modifier,
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun createPreviewWebView(context: Context): WebView = WebView(context).apply {
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.setSupportZoom(true)
    settings.builtInZoomControls = true
    settings.displayZoomControls = false
    webViewClient = WebViewClient()
}
