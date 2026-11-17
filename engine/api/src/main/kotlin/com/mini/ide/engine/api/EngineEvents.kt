package com.mini.ide.engine.api

import com.mini.ide.core.model.Project
import com.mini.ide.core.model.SourceFile

/** 编辑器事件流。 */
sealed interface EditorEvent {
    data class ContentChanged(val fileId: String, val content: String) : EditorEvent
    data class CursorMoved(val line: Int, val column: Int) : EditorEvent
    data class Ready(val fileId: String) : EditorEvent
}

enum class LogLevel { LOG, WARN, ERROR }

/** 渲染/预览事件流。 */
sealed interface RenderEvent {
    data class Console(val level: LogLevel, val message: String, val line: Int) : RenderEvent
    data class RuntimeError(val message: String, val source: String, val line: Int) : RenderEvent
    data class Completed(val durationMs: Long) : RenderEvent
}

/** 一次预览渲染的输入文档（三文件 + 沙箱基址）。 */
data class PreviewDocument(
    val html: String,
    val css: String,
    val js: String,
    val baseUrl: String,
    val sandboxFlags: Int = 0,
)

/** 项目引擎对外暴露的单一真相状态。 */
data class ProjectState(
    val project: Project,
    val files: List<SourceFile>,
    val activeFileId: String,
    val isDirty: Boolean,
)
