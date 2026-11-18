package com.mini.ide.feature.preview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.mini.ide.engine.api.WebViewPort
import org.json.JSONObject

/**
 * 预览 WebView 的 WebViewPort 适配。
 *
 * 线程契约：WebView 的所有方法必须在主线程（创建它的 Looper 线程）调用，
 * 否则抛 `A WebView method was called on thread ...`（rc1 crash 复盘）。
 * 本适配层作为兜底防线：无论调用方来自 IO/Default/JS 线程，统一切主线程执行，
 * 让上层 ViewModel/引擎无需关心 WebView 的线程亲和性。
 */
class PreviewWebViewPort(val webView: WebView) : WebViewPort {

    /** 已在主线程则同步执行，否则 post 到主线程队列。 */
    private inline fun onMain(crossinline block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else webView.post { block() }
    }

    override fun addJavascriptInterface(obj: Any, name: String) = onMain {
        webView.addJavascriptInterface(obj, name)
    }

    override fun evaluateJavascript(script: String, callback: ((String?) -> Unit)?) = onMain {
        webView.evaluateJavascript(script, callback)
    }

    override fun loadDataWithBaseUrl(baseUrl: String?, data: String, mimeType: String, encoding: String, historyUrl: String?) = onMain {
        webView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    override fun reload() = onMain { webView.reload() }
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
