package com.mini.ide.feature.editor

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.mini.ide.engine.api.WebViewPort

/**
 * WebViewPort 的真实 Android 适配 —— 设计说明决策 2。
 * 包装 android.webkit.WebView，实现引擎侧的纯 Kotlin 端口；
 * 所有方法假定在主线程调用（Compose AndroidView / ViewModel main dispatcher）。
 */
class AndroidWebViewPort(val webView: WebView) : WebViewPort {
    override fun addJavascriptInterface(obj: Any, name: String) {
        webView.addJavascriptInterface(obj, name)
    }

    @SuppressLint("JavascriptInterface")
    override fun evaluateJavascript(script: String, callback: ((String?) -> Unit)?) {
        webView.evaluateJavascript(script, callback)
    }

    override fun loadDataWithBaseUrl(
        baseUrl: String?, data: String, mimeType: String, encoding: String, historyUrl: String?,
    ) {
        webView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    override fun reload() = webView.reload()
}

/**
 * 编辑器 WebView Composable。创建 WebView 后回调 [onPortReady] 把适配端口交给 ViewModel 绑定引擎。
 */
@Composable
fun EditorWebView(
    onPortReady: (WebViewPort) -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context: Context -> createEditorWebView(context).also { onPortReady(AndroidWebViewPort(it)) } },
        modifier = modifier,
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun createEditorWebView(context: Context): WebView = WebView(context).apply {
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.setSupportZoom(false)
    settings.builtInZoomControls = false
    webViewClient = WebViewClient()
    loadUrl("file:///android_asset/editor/index.html")
}
