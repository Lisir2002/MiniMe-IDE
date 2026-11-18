package com.mini.ide.engine.impl

import com.mini.ide.core.model.CheckContext
import com.mini.ide.core.model.FileKind
import com.mini.ide.core.model.SourceFile
import com.mini.ide.core.model.ValidatorSpec
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** M5 步骤校验器单测：Contains / Regex / AstRule 三路径及反馈文案。 */
class StepValidatorTest {

    private fun files(vararg contents: Pair<FileKind, String>) = CheckContext(
        contents.mapIndexed { i, (kind, content) ->
            SourceFile("f$i", "p", "f$i", kind, content, i)
        }
    )

    @Test
    fun `Contains 命中返回通过`() {
        val ctx = files(FileKind.HTML to "<body><h1>你好</h1></body>")
        val r = StepValidator.validate(ValidatorSpec.Contains("<h1"), ctx)
        assertTrue(r.passed)
        assertTrue(r.feedback.isNotBlank())
    }

    @Test
    fun `Contains 未命中返回失败且反馈含目标`() {
        val ctx = files(FileKind.HTML to "<p>hi</p>")
        val r = StepValidator.validate(ValidatorSpec.Contains("<h1"), ctx)
        assertFalse(r.passed)
        assertTrue(r.feedback.contains("<h1"))
    }

    @Test
    fun `Regex 忽略大小写命中`() {
        val ctx = files(FileKind.CSS to "h1 { color: #0a84ff; }")
        val r = StepValidator.validate(ValidatorSpec.Regex("#0A84FF", ignoreCase = true), ctx)
        assertTrue(r.passed)
    }

    @Test
    fun `Regex 区分大小写时未命中`() {
        val ctx = files(FileKind.CSS to "h1 { color: #0a84ff; }")
        val r = StepValidator.validate(ValidatorSpec.Regex("#0A84FF", ignoreCase = false), ctx)
        assertFalse(r.passed)
    }

    @Test
    fun `非法正则不崩溃且判失败`() {
        val ctx = files(FileKind.JS to "var a = 1;")
        val r = StepValidator.validate(ValidatorSpec.Regex("[invalid("), ctx)
        assertFalse(r.passed)
    }

    @Test
    fun `AstRule previewRun 教学向默认放行并引导运行`() {
        val ctx = files(FileKind.HTML to "<h1>x</h1>")
        val r = StepValidator.validate(ValidatorSpec.AstRule("previewRun"), ctx)
        assertTrue(r.passed)
        assertTrue(r.feedback.contains("运行"))
    }

    @Test
    fun `未知 AstRule 按大括号结构启发式判定`() {
        val withBraces = files(FileKind.CSS to "h1 { color: red; }")
        assertTrue(StepValidator.validate(ValidatorSpec.AstRule("hasBlock"), withBraces).passed)
        val noBraces = files(FileKind.JS to "var a = 1;")
        assertFalse(StepValidator.validate(ValidatorSpec.AstRule("hasBlock"), noBraces).passed)
    }
}
