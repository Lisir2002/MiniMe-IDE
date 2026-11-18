package com.mini.ide.language.dsl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DslCodeGeneratorTest {

    @Test
    fun `生成完整HTML骨架与标题`() {
        val result = DslParser().parse("页面 \"我的网页\"")
        val gen = DslCodeGenerator.generate(result.statements)
        assertTrue(gen.html.contains("<!DOCTYPE html>"))
        assertTrue(gen.html.contains("<title>我的网页</title>"))
        assertTrue(gen.html.contains("minilang-title"))
        // 骨架 HTML 不含内联 style，CSS 独立返回交由 builder 注入
        assertFalse(gen.html.contains("<style>"))
    }

    @Test
    fun `文本被HTML转义`() {
        val result = DslParser().parse("文字 \"<script>alert(1)</script>\"")
        val gen = DslCodeGenerator.generate(result.statements)
        assertTrue(gen.html.contains("&lt;script&gt;"))
        assertFalse(gen.html.contains("<script>alert(1)"))
    }

    @Test
    fun `颜色词表映射与十六进制透传大写`() {
        val result = DslParser().parse(
            """
            文字 "A" 颜色 "红"
            文字 "B" 颜色 "#0a84ff"
            """.trimIndent(),
        )
        val gen = DslCodeGenerator.generate(result.statements)
        assertTrue(gen.html.contains("color:#FF453A"))
        assertTrue(gen.html.contains("color:#0A84FF"))
    }

    @Test
    fun `点击事件生成按钮匹配脚本`() {
        val result = DslParser().parse(
            """
            按钮 "点我"
            当点击 "点我" 显示 "你好"
            """.trimIndent(),
        )
        val gen = DslCodeGenerator.generate(result.statements)
        assertTrue(gen.js.contains("addEventListener('click'"))
        assertTrue(gen.js.contains("点我"))
        assertTrue(gen.js.contains("你好"))
        assertTrue(gen.warnings.isEmpty())
    }

    @Test
    fun `点击不存在的按钮产生行号警告`() {
        val result = DslParser().parse("当点击 " + "\"幽灵\"" + " 显示 \"x\"")
        val gen = DslCodeGenerator.generate(result.statements)
        assertEquals(1, gen.warnings.size)
        assertEquals(1, gen.warnings.first().first)
    }

    @Test
    fun `背景设置写入CSS`() {
        val result = DslParser().parse("设置 背景 \"深蓝\"")
        val gen = DslCodeGenerator.generate(result.statements)
        assertTrue(gen.css.contains("background:#1C3A70"))
    }

    @Test
    fun `卡片递归渲染子内容`() {
        val result = DslParser().parse(
            """
            卡片 "介绍" {
              文字 "内容"
            }
            """.trimIndent(),
        )
        val gen = DslCodeGenerator.generate(result.statements)
        assertTrue(gen.html.contains("minilang-card"))
        assertTrue(gen.html.contains("内容"))
    }
}
