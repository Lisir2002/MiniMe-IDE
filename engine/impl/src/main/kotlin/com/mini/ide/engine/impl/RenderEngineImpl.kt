package com.mini.ide.engine.impl

import com.mini.ide.engine.api.BridgeHandler
import com.mini.ide.engine.api.BridgeMessage
import com.mini.ide.engine.api.LogLevel
import com.mini.ide.engine.api.PreviewDocument
import com.mini.ide.engine.api.RenderEngine
import com.mini.ide.engine.api.RenderEvent
import com.mini.ide.engine.api.WebViewPort
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 渲染引擎实现：拼装预览页并加载进 WebView，接收 console/error 回传。
 * 只依赖 [WebViewPort] 与桥接消息回调，可用 FakeWebViewPort 在 JVM 单测。
 */
class RenderEngineImpl : RenderEngine, BridgeHandler {

    private var port: WebViewPort? = null
    private var lastDocument: PreviewDocument? = null

    private val _events = MutableSharedFlow<RenderEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<RenderEvent> = _events.asSharedFlow()

    override fun bind(port: WebViewPort) {
        this.port = port
    }

    override fun render(document: PreviewDocument) {
        lastDocument = document
        val start = System.currentTimeMillis()
        port?.loadDataWithBaseUrl(
            document.baseUrl,
            document.html,
            "text/html",
            "UTF-8",
            null,
        )
        _events.tryEmit(RenderEvent.Completed(System.currentTimeMillis() - start))
    }

    override fun reload() {
        port?.reload()
    }

    /** 接收桥接回传的渲染类消息。 */
    override fun onMessage(type: BridgeMessage, payload: String) {
        when (type) {
            BridgeMessage.CONSOLE -> {
                // payload 约定 "LEVEL|line|message"
                val parts = payload.split("|", limit = 3)
                if (parts.size == 3) {
                    val level = runCatching { LogLevel.valueOf(parts[0]) }.getOrDefault(LogLevel.LOG)
                    val line = parts[1].toIntOrNull() ?: 0
                    _events.tryEmit(RenderEvent.Console(level, parts[2], line))
                }
            }
            BridgeMessage.RUNTIME_ERROR -> {
                val parts = payload.split("|", limit = 3)
                if (parts.size == 3) {
                    _events.tryEmit(RenderEvent.RuntimeError(parts[2], parts[1], parts[0].toIntOrNull() ?: 0))
                }
            }
            BridgeMessage.RENDER_DONE -> {
                // duration 由 render() 本地计时，此处忽略重复信号
            }
            else -> Unit
        }
    }

    override fun release() {
        port = null
        lastDocument = null
    }
}
