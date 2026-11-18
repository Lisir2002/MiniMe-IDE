package com.mini.ide.engine.api

/**
 * 结构化教学诊断（M5 错误提示增强）。
 * 把原始 RenderEvent 升级为"分类 + 中文建议 + 行号"的可理解信息。
 */

enum class DiagnosticCategory {
    RUNTIME,   // JS 运行时错误
    SYNTAX,    // 语法错误
    NETWORK,   // 资源加载 404 等
    RESOURCE,  // 资源引用
    INFO,      // 普通日志
    UNKNOWN,   // 兜底
}

data class Diagnostic(
    val category: DiagnosticCategory,
    val severity: LogLevel,
    val title: String,
    val detail: String,
    val suggestion: String?,
    val source: String = "",
    val line: Int = 0,
)

/**
 * 诊断引擎：把预览/渲染事件转为结构化诊断。
 * 纯 JVM 可测，零 Android 依赖。
 */
interface DiagnosticEngine {
    fun diagnose(event: RenderEvent, currentDoc: PreviewDocument? = null): Diagnostic
}
