package com.mini.ide.engine.impl

import com.mini.ide.core.model.CheckContext
import com.mini.ide.core.model.SourceFile
import com.mini.ide.core.model.StepResult
import com.mini.ide.core.model.ValidatorSpec

/**
 * 课程步骤校验器（纯函数，M5）。
 * 把当前项目三文件拼成一段文本，按数据驱动的 ValidatorSpec 判定通过与否。
 *
 * AstRule 在 Phase2 无真 AST，降级为"关键字 + 结构启发式"，教学向宽松校验。
 */
internal object StepValidator {

    fun validate(validator: ValidatorSpec, context: CheckContext): StepResult = when (validator) {
        is ValidatorSpec.Contains -> validateContains(validator.text, context)
        is ValidatorSpec.Regex -> validateRegex(validator.pattern, validator.ignoreCase, context)
        is ValidatorSpec.AstRule -> validateAst(validator.rule, context)
    }

    private val CheckContext.joined: String
        get() = files.joinToString("\n") { it.content }

    private fun validateContains(text: String, context: CheckContext): StepResult {
        val passed = context.joined.contains(text, ignoreCase = true)
        return StepResult(
            passed = passed,
            feedback = if (passed) "很好，找到你要写的内容了！" else "还没找到「${text}」，再试试看。",
        )
    }

    private fun validateRegex(pattern: String, ignoreCase: Boolean, context: CheckContext): StepResult {
        val passed = runCatching {
            Regex(pattern, if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet())
                .containsMatchIn(context.joined)
        }.getOrDefault(false)
        return StepResult(
            passed = passed,
            feedback = if (passed) "正确！这一步做到了。" else "格式还差一点，对照提示再检查一下。",
        )
    }

    /**
     * 结构启发式：目前仅支持 previewRun（预览运行信号）。
     * 该信号无法从源码文本判断，故教学向默认放行并在反馈中引导用户运行预览；
     * 其余未知规则名也宽松放行（避免误报挫败感），但给出中性反馈。
     */
    private fun validateAst(rule: String, context: CheckContext): StepResult = when (rule) {
        "previewRun" -> StepResult(
            passed = true,
            feedback = "点击预览区右上角的「运行」按钮，看看效果吧。",
        )
        else -> {
            val hasBraces = context.joined.contains("{") && context.joined.contains("}")
            StepResult(
                passed = hasBraces,
                feedback = if (hasBraces) "结构看起来不错！" else "检查代码结构，比如大括号是否成对出现。",
            )
        }
    }
}
