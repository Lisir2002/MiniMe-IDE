package com.mini.ide.feature.editor

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

/**
 * WebViewPort 的真实 Android 适配 —— 设计说明决策 2。
 * 包装 android.webkit.WebView，实现引擎侧的纯 Kotlin 端口。
 *
 * 线程契约：WebView 方法必须在主线程调用。本适配层作为兜底防线——
 * 已在主线程则同步执行（保留 evaluateJavascript callback 的同步语义），
 * 否则 post 到主线程，使上层无需关心线程亲和性（rc1 crash 复盘）。
 */
class AndroidWebViewPort(val webView: WebView) : WebViewPort {

    private inline fun onMain(crossinline block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else webView.post { block() }
    }

    override fun addJavascriptInterface(obj: Any, name: String) = onMain {
        webView.addJavascriptInterface(obj, name)
    }

    @SuppressLint("JavascriptInterface")
    override fun evaluateJavascript(script: String, callback: ((String?) -> Unit)?) = onMain {
        webView.evaluateJavascript(script, callback)
    }

    override fun loadDataWithBaseUrl(
        baseUrl: String?, data: String, mimeType: String, encoding: String, historyUrl: String?,
    ) = onMain {
        webView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    override fun reload() = onMain { webView.reload() }
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
