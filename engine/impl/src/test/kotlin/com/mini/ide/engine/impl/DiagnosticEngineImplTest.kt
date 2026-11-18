package com.mini.ide.engine.impl

import com.mini.ide.engine.api.DiagnosticCategory
import com.mini.ide.engine.api.LogLevel
import com.mini.ide.engine.api.RenderEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** M5 错误诊断引擎单测：规则命中、分类、建议、兜底、行号透传。 */
class DiagnosticEngineImplTest {

    private val engine = DiagnosticEngineImpl()

    @Test
    fun `is not defined 命中运行时错误并给建议`() {
        val d = engine.diagnose(
            RenderEvent.RuntimeError("ReferenceError: foo is not defined", "main.js", 12)
        )
        assertEquals(DiagnosticCategory.RUNTIME, d.category)
        assertEquals(LogLevel.ERROR, d.severity)
        assertEquals(12, d.line)
        assertEquals("main.js", d.source)
        assertNotNull(d.suggestion)
        assertTrue(d.suggestion!!.contains("声明"))
    }

    @Test
    fun `语法错误命中 SYNTAX 分类`() {
        val d = engine.diagnose(
            RenderEvent.RuntimeError("SyntaxError: Unexpected token", "main.js", 5)
        )
        assertEquals(DiagnosticCategory.SYNTAX, d.category)
        assertTrue(d.title.contains("语法"))
    }

    @Test
    fun `404 命中网络分类`() {
        val d = engine.diagnose(
            RenderEvent.RuntimeError("Failed to load resource: 404", "style.css", 0)
        )
        assertEquals(DiagnosticCategory.NETWORK, d.category)
        assertTrue(d.suggestion!!.contains("文件名"))
    }

    @Test
    fun `Cannot read property 命中属性访问错误`() {
        val d = engine.diagnose(
            RenderEvent.RuntimeError("TypeError: Cannot read properties of null", "main.js", 8)
        )
        assertEquals(DiagnosticCategory.RUNTIME, d.category)
        assertTrue(d.title.contains("属性"))
    }

    @Test
    fun `未知错误走兜底但仍给建议`() {
        val d = engine.diagnose(RenderEvent.RuntimeError("Something odd happened", "main.js", 3))
        assertEquals(DiagnosticCategory.UNKNOWN, d.category)
        assertNotNull(d.suggestion)
        assertTrue(d.suggestion!!.isNotBlank())
    }

    @Test
    fun `普通 console log 归为 INFO 且无建议`() {
        val d = engine.diagnose(RenderEvent.Console(LogLevel.LOG, "hello", 1))
        assertEquals(DiagnosticCategory.INFO, d.category)
        assertEquals(LogLevel.LOG, d.severity)
        assertEquals(null, d.suggestion)
    }

    @Test
    fun `console warn 保留 WARN 级别`() {
        val d = engine.diagnose(RenderEvent.Console(LogLevel.WARN, "careful", 2))
        assertEquals(LogLevel.WARN, d.severity)
    }

    @Test
    fun `渲染完成事件归为 INFO`() {
        val d = engine.diagnose(RenderEvent.Completed(42L))
        assertEquals(DiagnosticCategory.INFO, d.category)
        assertTrue(d.detail.contains("42"))
    }
}
