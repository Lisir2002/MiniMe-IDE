package com.mini.ide.engine.api

import com.mini.ide.core.model.EditorTheme
import com.mini.ide.language.api.LanguageProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

/** ① 编辑引擎：封装编辑器，持有当前文件与光标状态。实现同时是桥接消息处理器。 */
interface EditorEngine : BridgeHandler {
    /** 绑定 WebView 端口、语言配置与主题（生命周期内一次）。 */
    fun bind(port: WebViewPort, language: LanguageProfile, theme: EditorTheme)

    /** 载入某个文件内容与语言。 */
    fun load(fileId: String, content: String, language: LanguageProfile)

    /** 同步读取当前编辑器文本（JS 调用返回）。 */
    fun currentContent(): String

    fun insertText(text: String)
    fun undo()
    fun redo()
    fun setTheme(theme: EditorTheme)
    fun setFontSize(sp: Int)
    fun setWordWrap(enabled: Boolean)
    val events: SharedFlow<EditorEvent>
    fun release()
}

/** ② 渲染引擎：把三文件拼成预览页并注入 WebView。实现同时是桥接消息处理器。 */
interface RenderEngine : BridgeHandler {
    fun bind(port: WebViewPort)
    fun render(document: PreviewDocument)
    fun reload()
    val events: SharedFlow<RenderEvent>
    fun release()
}

/** ③ 桥接引擎：JS↔Kotlin 唯一通道，封装所有 evaluateJavascript 与消息解析。 */
interface BridgeEngine {
    fun install(port: WebViewPort, handler: BridgeHandler)

    /** 统一出口：把方法调用序列化为 JS dispatch，返回 JS 执行结果。 */
    fun call(method: BridgeMethod, vararg args: Any): String?

    /** 供 @JavascriptInterface 回调：解析 JSON 分发给 handler。 */
    fun receive(json: String)

    fun release()
}

/** ④ 项目引擎：编排数据层与编辑/渲染层，持有活跃项目单一真相。 */
interface ProjectEngine {
    fun open(projectId: String): Flow<ProjectState>
    fun observeActive(): Flow<ProjectState?>
    suspend fun saveActive()
    fun switchFile(fileId: String)
    fun markContentDirty(fileId: String, content: String)
    fun currentPreviewDocument(baseUrl: String): PreviewDocument?
    fun close()
}
