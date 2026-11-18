package com.mini.ide.language.dsl

import com.mini.ide.language.api.CompileSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DslCompilerImplTest {

    private val compiler = DslCompilerImpl()

    @Test
    fun `端到端编译产出html css js`() {
        val result = compiler.compile(
            """
            页面 "我的网页"
            文字 "你好，MiniMe！"
            按钮 "点我"
            当点击 "点我" 显示 "你点到我了"
            """.trimIndent(),
        )
        assertTrue(result.html.contains("我的网页"))
        assertTrue(result.html.contains("你好，MiniMe！"))
        assertTrue(result.html.contains("点我"))
        assertTrue(result.css.contains("minilang-button"))
        assertTrue(result.js.contains("你点到我了"))
        assertTrue(result.diagnostics.isEmpty())
    }

    @Test
    fun `语法错误不中断其余行并给出行号`() {
        val result = compiler.compile(
            """
            页面 "A"
            乱七八糟
            文字 "B"
            """.trimIndent(),
        )
        assertTrue(result.html.contains("B"))
        assertTrue(result.diagnostics.any { it.line == 2 && it.severity == CompileSeverity.ERROR })
    }

    @Test
    fun `诊断按行号排序`() {
        val result = compiler.compile(
            """
            乱写1
            乱写2
            """.trimIndent(),
        )
        val lines = result.diagnostics.map { it.line }
        assertEquals(lines.sorted(), lines)
    }

    @Test
    fun `空源码给出提示而非错误`() {
        val result = compiler.compile("")
        assertTrue(result.diagnostics.any { it.severity == CompileSeverity.WARNING })
        assertFalse(result.diagnostics.any { it.severity == CompileSeverity.ERROR })
    }

    @Test
    fun `重复编译同一源码结果一致(幂等)`() {
        val source = "页面 \"X\"\n文字 \"Y\""
        val a = compiler.compile(source)
        val b = compiler.compile(source)
        assertEquals(a.html, b.html)
        assertEquals(a.css, b.css)
        assertEquals(a.js, b.js)
    }

    @Test
    fun `点击不存在按钮的警告进入诊断`() {
        val result = compiler.compile("当点击 \"幽灵\" 显示 \"x\"")
        assertTrue(result.diagnostics.any { it.line == 1 && it.message.contains("不存在") })
    }

    @Test
    fun `profile返回MiniLang档案`() {
        assertEquals("MiniLang", compiler.profile().displayName)
    }
}
