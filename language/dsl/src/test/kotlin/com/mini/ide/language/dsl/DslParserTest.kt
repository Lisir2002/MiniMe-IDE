package com.mini.ide.language.dsl

import com.mini.ide.language.api.CompileSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DslParserTest {

    private val parser = DslParser()

    @Test
    fun `解析页面文字按钮三种语句`() {
        val result = parser.parse(
            """
            页面 "我的网页"
            文字 "你好"
            按钮 "点我"
            """.trimIndent(),
        )
        assertEquals(3, result.statements.size)
        assertTrue(result.statements[0] is PageStatement)
        assertEquals("我的网页", (result.statements[0] as PageStatement).title)
        assertEquals(ElementType.TEXT, (result.statements[1] as ElementStatement).type)
        assertEquals(ElementType.BUTTON, (result.statements[2] as ElementStatement).type)
        assertTrue(result.diagnostics.isEmpty())
    }

    @Test
    fun `解析元素属性颜色与大小`() {
        val result = parser.parse("标题 \"欢迎\" 颜色 \"蓝色\" 大小 \"大\"")
        val element = result.statements.first() as ElementStatement
        assertEquals("蓝色", element.attributes["颜色"])
        assertEquals("大", element.attributes["大小"])
    }

    @Test
    fun `解析卡片嵌套子语句`() {
        val result = parser.parse(
            """
            卡片 "介绍" {
              文字 "第一段"
              文字 "第二段"
            }
            """.trimIndent(),
        )
        val card = result.statements.first() as ElementStatement
        assertEquals(ElementType.CARD, card.type)
        assertEquals("介绍", card.text)
        assertEquals(2, card.children.size)
        assertTrue(card.children.all { it is ElementStatement })
    }

    @Test
    fun `解析点击事件与背景设置`() {
        val result = parser.parse(
            """
            当点击 "点我" 显示 "你好"
            设置 背景 "深蓝"
            """.trimIndent(),
        )
        val click = result.statements.first() as ClickStatement
        assertEquals("点我", click.buttonText)
        assertEquals("你好", click.message)
        val setting = result.statements[1] as SettingStatement
        assertEquals("背景", setting.key)
        assertEquals("深蓝", setting.value)
    }

    @Test
    fun `未知语句产生诊断但不中断其他语句`() {
        val result = parser.parse(
            """
            页面 "A"
            随便写点什么
            文字 "B"
            """.trimIndent(),
        )
        // 未知行被跳过，页面与文字仍被解析
        assertEquals(2, result.statements.size)
        assertTrue(result.diagnostics.any { it.line == 2 && it.severity == CompileSeverity.ERROR })
    }

    @Test
    fun `缺少引号参数产生带行号诊断`() {
        val result = parser.parse("文字 没有引号")
        assertTrue(result.diagnostics.any { it.line == 1 && it.message.contains("缺少引号") })
        assertTrue(result.statements.isEmpty())
    }

    @Test
    fun `未知颜色词给出WARNING但仍保留属性`() {
        val result = parser.parse("文字 \"X\" 颜色 \"荧光粉\"")
        val element = result.statements.first() as ElementStatement
        assertEquals("荧光粉", element.attributes["颜色"])
        assertTrue(result.diagnostics.any { it.severity == CompileSeverity.WARNING })
    }

    @Test
    fun `深度嵌套触发上限保护不崩溃`() {
        val src = (1..50).joinToString("\n") { "${"  ".repeat(it)}卡片 \"L$it\" {" } +
            "\n" + (1..50).joinToString("\n") { "}" }
        val result = parser.parse(src)
        // 不抛异常即可，诊断可有可无
        assertTrue(result.statements.size <= 50)
    }
}
