package com.mini.ide.engine.impl

import com.mini.ide.engine.api.BridgeContract
import com.mini.ide.engine.api.BridgeEngine
import com.mini.ide.engine.api.BridgeHandler
import com.mini.ide.engine.api.BridgeMessage
import com.mini.ide.engine.api.BridgeMethod
import com.mini.ide.engine.api.WebViewPort

/**
 * 桥接引擎实现：JS↔Kotlin 的唯一通道。
 * - 所有 evaluateJavascript 集中在此，禁止外部散落调用；
 * - JS→Kotlin 消息经 [receive] 解析后交给 [handler]；
 * - 非线程安全？receive 由 WebView 线程回调，call 由调用方线程发起，内部无共享可变状态，OK。
 */
class BridgeEngineImpl : BridgeEngine {

    private var port: WebViewPort? = null
    private var handler: BridgeHandler? = null

    override fun install(port: WebViewPort, handler: BridgeHandler) {
        this.port = port
        this.handler = handler
        port.addJavascriptInterface(JsBridge(this), BridgeContract.JS_INTERFACE_NAME)
    }

    override fun call(method: BridgeMethod, vararg args: Any): String? {
        val script = BridgeCodec.encodeCall(method, args.toList())
        var result: String? = null
        port?.evaluateJavascript(script) { result = it }
        return result
    }

    /** 由 [JsBridge] 从 WebView JS 线程回调。解析失败静默忽略，保证桥接健壮性。 */
    override fun receive(json: String) {
        val decoded = runCatching { BridgeCodec.decodeMessage(json) }.getOrNull() ?: return
        handler?.onMessage(decoded.type, decoded.payload)
    }

    override fun release() {
        handler = null
        port = null
    }

    /** 注入到 JS 的桥接对象。@JavascriptInterface 的方法名即 JS 侧调用名。 */
    class JsBridge(private val engine: BridgeEngineImpl) {
        @android.webkit.JavascriptInterface
        fun post(json: String) {
            engine.receive(json)
        }
    }
}
