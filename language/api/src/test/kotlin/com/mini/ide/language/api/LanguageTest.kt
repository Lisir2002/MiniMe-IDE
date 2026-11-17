package com.mini.ide.language.api

import org.junit.Assert.assertEquals
import org.junit.Test

/** M0 契约测试：验证语言标识与 LanguageProfile 可插拔契约。 */
class LanguageTest {

    @Test
    fun `语言标识覆盖 HTML CSS JS 与 DSL`() {
        assertEquals(4, LanguageId.entries.size)
        assertEquals(
            setOf(LanguageId.HTML, LanguageId.CSS, LanguageId.JS, LanguageId.DSL),
            LanguageId.entries.toSet(),
        )
    }

    @Test
    fun `LanguageProfile 契约可由任意实现满足（可插拔性）`() {
        // 用一个内联匿名实现模拟 Phase3 的自研语言 Profile，验证接口不绑定具体实现
        val custom = object : LanguageProfile {
            override val id = LanguageId.DSL
            override val displayName = "MiniMe DSL"
            override val fileExtensions = listOf("mini")
            override val codemirrorMode = "text/x-minime"
            override val highlight = HighlightSpec(keywords = setOf("let", "show"))
            override val completions = emptyList<CompletionItem>()
            override val snippets = emptyList<Snippet>()
            override val brackets = listOf(BracketPair('(', ')'))
            override val autoCloseChars = setOf('"')
        }
        assertEquals(LanguageId.DSL, custom.id)
        assertEquals("MiniMe DSL", custom.displayName)
        assertEquals(listOf("mini"), custom.fileExtensions)
    }

    @Test
    fun `补全与片段数据类可由教学向配置构造`() {
        val item = CompletionItem("div", CompletionKind.TAG, "<div></div>", "块级容器")
        val snippet = Snippet("for", "for(\$1){\$0}", "循环", SnippetScope.JS)
        assertEquals("div", item.label)
        assertEquals(CompletionKind.TAG, item.kind)
        assertEquals(SnippetScope.JS, snippet.scope)
    }
}
