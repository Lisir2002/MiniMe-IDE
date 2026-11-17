package com.mini.ide.engine.api

/** Kotlin 侧主动调用 JS 的方法（统一出口 window.__ide.dispatch）。 */
enum class BridgeMethod {
    SET_CONTENT, SET_THEME, SET_LANGUAGE, FOCUS, INSERT_TEXT,
    UNDO, REDO, SET_WORD_WRAP, SET_FONT_SIZE,
}

/** JS 侧回传给 Kotlin 的消息类型（统一入口 window.AndroidBridge.post）。 */
enum class BridgeMessage {
    CONTENT_CHANGED, CURSOR_MOVED, READY, CONSOLE, RUNTIME_ERROR, RENDER_DONE,
}

/** JS→Kotlin 单一入口回调。payload 约定见各消息：文本类为 Base64。 */
fun interface BridgeHandler {
    fun onMessage(type: BridgeMessage, payload: String)
}

/** 桥接常量：两端共享的字符串契约，避免散落魔法值。 */
object BridgeContract {
    const val JS_INTERFACE_NAME = "AndroidBridge"
    const val JS_DISPATCH = "window.__ide.dispatch"
    const val ENTRY_POST = "post"
}
