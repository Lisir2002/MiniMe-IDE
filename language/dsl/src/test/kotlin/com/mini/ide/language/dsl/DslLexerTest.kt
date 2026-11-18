package com.mini.ide.language.dsl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DslLexerTest {

    @Test
    fun `关键字与字符串被正确切分`() {
        val tokens = DslLexer.tokenize("页面 \"你好\"")
        assertEquals(DslTokenType.WORD, tokens[0].type)
        assertEquals("页面", tokens[0].value)
        assertEquals(DslTokenType.STRING, tokens[1].type)
        assertEquals("你好", tokens[1].value)
        assertEquals(DslTokenType.EOF, tokens[2].type)
    }

    @Test
    fun `注释被丢弃并保留换行与行号`() {
        val tokens = DslLexer.tokenize("页面 \"A\" # 这是注释\n文字 \"B\"")
        val words = tokens.filter { it.type == DslTokenType.WORD }.map { it.value }
        assertEquals(listOf("页面", "文字"), words)
        assertTrue(tokens.any { it.type == DslTokenType.NEWLINE })
        assertEquals(2, tokens.first { it.value == "文字" }.line)
    }

    @Test
    fun `转义引号与反斜杠被还原`() {
        val tokens = DslLexer.tokenize("文字 \"说\\\"你好\\\"\\\\ok\"")
        val text = tokens.first { it.type == DslTokenType.STRING }.value
        assertEquals("说\"你好\"\\ok", text)
    }

    @Test
    fun `未闭合字符串被标记`() {
        val tokens = DslLexer.tokenize("文字 \"没有结束")
        assertTrue(tokens.any { it.type == DslTokenType.UNCLOSED_STRING })
    }

    @Test
    fun `花括号作为块定界符产生独立token`() {
        val tokens = DslLexer.tokenize("卡片 \"X\" { 文字 \"Y\" }")
        assertEquals(1, tokens.count { it.type == DslTokenType.LBRACE })
        assertEquals(1, tokens.count { it.type == DslTokenType.RBRACE })
    }
}
