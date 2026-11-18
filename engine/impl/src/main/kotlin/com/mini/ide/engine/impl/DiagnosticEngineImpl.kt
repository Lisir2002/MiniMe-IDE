package com.mini.ide.engine.impl

import com.mini.ide.engine.api.Diagnostic
import com.mini.ide.engine.api.DiagnosticCategory
import com.mini.ide.engine.api.DiagnosticEngine
import com.mini.ide.engine.api.LogLevel
import com.mini.ide.engine.api.PreviewDocument
import com.mini.ide.engine.api.RenderEvent

/**
 * 诊断引擎实现（M5）：把原始渲染事件转为结构化教学诊断。
 *
 * - RUNTIME_ERROR：拿 message 逐条匹配规则表，命中则给分类 + 建议；否则 UNKNOWN 兜底；
 * - Console(WARN/ERROR)：保留级别，尝试匹配建议；普通 LOG 归 INFO；
 * - currentDoc 预留给未来的"错误行 ↔ 源码文件"映射，当前不影响分类（避免误报）。
 *
 * 纯函数式、无 Android 依赖，可全 JVM 单测。
 */
class DiagnosticEngineImpl : DiagnosticEngine {

    override fun diagnose(event: RenderEvent, currentDoc: PreviewDocument?): Diagnostic = when (event) {
        is RenderEvent.RuntimeError -> fromRuntime(event)
        is RenderEvent.Console -> fromConsole(event)
        is RenderEvent.Completed -> Diagnostic(
            category = DiagnosticCategory.INFO,
            severity = LogLevel.LOG,
            title = "预览已刷新",
            detail = "渲染耗时 ${event.durationMs}ms",
            suggestion = null,
        )
    }

    private fun fromRuntime(event: RenderEvent.RuntimeError): Diagnostic {
        val match = matchRule(event.message)
        return Diagnostic(
            category = match?.category ?: DiagnosticCategory.UNKNOWN,
            severity = LogLevel.ERROR,
            title = match?.title ?: "运行时出错了",
            detail = event.message,
            suggestion = match?.suggestion ?: FALLBACK_SUGGESTION,
            source = event.source,
            line = event.line,
        )
    }

    private fun fromConsole(event: RenderEvent.Console): Diagnostic {
        if (event.level == LogLevel.LOG) {
            return Diagnostic(
                category = DiagnosticCategory.INFO,
                severity = LogLevel.LOG,
                title = "控制台输出",
                detail = event.message,
                suggestion = null,
                line = event.line,
            )
        }
        val match = matchRule(event.message)
        return Diagnostic(
            category = match?.category ?: DiagnosticCategory.UNKNOWN,
            severity = event.level,
            title = match?.title ?: if (event.level == LogLevel.WARN) "有一条警告" else "控制台报错",
            detail = event.message,
            suggestion = match?.suggestion,
            line = event.line,
        )
    }

    /** 逐条匹配关键词，命中第一条即返回（规则表已按具体度排序）。 */
    private fun matchRule(message: String): DiagnosticRules.Rule? {
        val haystack = message.lowercase()
        return DiagnosticRules.rules.firstOrNull { rule ->
            rule.keywords.any { kw -> haystack.contains(if (rule.ignoreCase) kw.lowercase() else kw) }
        }
    }

    private companion object {
        const val FALLBACK_SUGGESTION = "对照控制台原文检查代码，或回看教程当前步骤的提示。"
    }
}
