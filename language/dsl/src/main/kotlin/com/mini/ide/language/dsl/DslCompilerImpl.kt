package com.mini.ide.language.dsl

import com.mini.ide.language.api.CompileDiagnostic
import com.mini.ide.language.api.CompileResult
import com.mini.ide.language.api.CompileSeverity
import com.mini.ide.language.api.DslCompiler
import com.mini.ide.language.api.LanguageProfile
import javax.inject.Inject

/**
 * MiniLang 编译器实现：parse → generate，合并两阶段诊断。
 *
 * 由 Hilt 通过 @Inject 构造，engine:impl 只依赖 DslCompiler 接口。
 * 纯 JVM 逻辑，可在局部穷举语法与错误分支。
 */
class DslCompilerImpl @Inject constructor() : DslCompiler {

    private val parser = DslParser()

    override fun compile(source: String): CompileResult {
        val parsed = parser.parse(source)
        val generated = DslCodeGenerator.generate(parsed.statements)
        val diagnostics = parsed.diagnostics.toMutableList()
        generated.warnings.forEach { (line, message) ->
            diagnostics += CompileDiagnostic(line, message, CompileSeverity.WARNING)
        }
        // 空源码给出一条 INFO 级提示（用 WARNING 复用枚举，避免误报为错误）
        if (source.isBlank()) {
            diagnostics += CompileDiagnostic(1, "还没有内容，试着写一句「页面 \"你好\"」", CompileSeverity.WARNING)
        }
        return CompileResult(
            html = generated.html,
            css = generated.css,
            js = generated.js,
            diagnostics = diagnostics.sortedBy { it.line },
        )
    }

    override fun profile(): LanguageProfile = DslProfile
}
