package com.mini.ide.engine.impl

import com.mini.ide.core.model.EditorTheme
import com.mini.ide.engine.api.BridgeEngine
import com.mini.ide.engine.api.BridgeHandler
import com.mini.ide.engine.api.BridgeMessage
import com.mini.ide.engine.api.BridgeMethod
import com.mini.ide.engine.api.EditorEngine
import com.mini.ide.engine.api.EditorEvent
import com.mini.ide.engine.api.WebViewPort
import com.mini.ide.language.api.LanguageProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 编辑引擎实现：封装 CodeMirror 6 的 Kotlin 侧状态。
 *
 * - WebView 内的 CM6 是真实编辑器；此类通过 [bridge] 下发命令，并镜像当前内容/光标；
 * - 内容变化由 JS 经 [BridgeMessage.CONTENT_CHANGED] 回传，这里解码后更新镜像并发事件；
 * - 只依赖 [BridgeEngine] 接口，可用 FakeBridge 在纯 JVM 上单测。
 */
class EditorEngineImpl(
    private val bridge: BridgeEngine,
) : EditorEngine, BridgeHandler {

    private var activeFileId: String = ""
    private var contentMirror: String = ""
    private var currentLanguage: LanguageProfile? = null

    private val _events = MutableSharedFlow<EditorEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<EditorEvent> = _events.asSharedFlow()

    override fun bind(port: WebViewPort, language: LanguageProfile, theme: EditorTheme) {
        // 安装桥接由上层统一负责；此处仅初始化 CM6 前置配置
        currentLanguage = language
        bridge.call(BridgeMethod.SET_LANGUAGE, language.codemirrorMode)
        setTheme(theme)
    }

    override fun load(fileId: String, content: String, language: LanguageProfile) {
        activeFileId = fileId
        currentLanguage = language
        contentMirror = content
        bridge.call(BridgeMethod.SET_CONTENT, BridgeCodec.encodeText(content))
        bridge.call(BridgeMethod.SET_LANGUAGE, language.codemirrorMode)
    }

    override fun currentContent(): String = contentMirror

    override fun insertText(text: String) {
        bridge.call(BridgeMethod.INSERT_TEXT, BridgeCodec.encodeText(text))
    }

    override fun undo() {
        bridge.call(BridgeMethod.UNDO)
    }

    override fun redo() {
        bridge.call(BridgeMethod.REDO)
    }

    override fun setTheme(theme: EditorTheme) {
        bridge.call(BridgeMethod.SET_THEME, ThemeSerializer.toJson(theme))
    }

    override fun setFontSize(sp: Int) {
        bridge.call(BridgeMethod.SET_FONT_SIZE, sp)
    }

    override fun setWordWrap(enabled: Boolean) {
        bridge.call(BridgeMethod.SET_WORD_WRAP, enabled)
    }

    /** 接收桥接消息（由上层把 EditorEngineImpl 注册为 BridgeHandler 之一）。 */
    override fun onMessage(type: BridgeMessage, payload: String) {
        when (type) {
            BridgeMessage.CONTENT_CHANGED -> {
                contentMirror = BridgeCodec.decodeText(payload)
                _events.tryEmit(EditorEvent.ContentChanged(activeFileId, contentMirror))
            }
            BridgeMessage.CURSOR_MOVED -> {
                // payload 约定 "line:column"
                val parts = payload.split(":")
                if (parts.size == 2) {
                    val line = parts[0].toIntOrNull() ?: 0
                    val column = parts[1].toIntOrNull() ?: 0
                    _events.tryEmit(EditorEvent.CursorMoved(line, column))
                }
            }
            BridgeMessage.READY -> _events.tryEmit(EditorEvent.Ready(activeFileId))
            else -> Unit // 渲染类消息由 RenderEngineImpl 处理
        }
    }

    override fun release() {
        contentMirror = ""
        activeFileId = ""
        currentLanguage = null
    }
}
