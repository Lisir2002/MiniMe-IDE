package com.mini.ide.language.api

/** 编译诊断严重级别。 */
enum class CompileSeverity { ERROR, WARNING }

/** 一条编译诊断，携带行号，供 UI 定位与教学建议。 */
data class CompileDiagnostic(
    val line: Int,
    val message: String,
    val severity: CompileSeverity = CompileSeverity.ERROR,
)

/**
 * 一次编译的产物。
 * html/css/js 直接喂给 PreviewDocumentBuilder.build(...)，与手写三文件走同一渲染链路；
 * diagnostics 携带行号，交由 UI 展示 / M5 诊断引擎升级为教学建议。
 */
data class CompileResult(
    val html: String,
    val css: String,
    val js: String,
    val diagnostics: List<CompileDiagnostic> = emptyList(),
)

/**
 * 自研语言编译器契约（纯 JVM，无 Android 依赖）。
 *
 * Phase3 可插拔核心：实现位于 :language:dsl，engine:impl 只依赖本接口并由 Hilt 注入，
 * 引擎层无需知道编译器具体实现。
 */
interface DslCompiler {
    /** 把 MiniLang 源文本编译为 html/css/js + 诊断；语法错误不中断，尽量产出可预览结果。 */
    fun compile(source: String): CompileResult

    /** 该编译器对应的语言档案（供编辑器获得高亮/补全）。 */
    fun profile(): LanguageProfile
}
