package com.mini.ide.language.impl

import com.mini.ide.language.api.BracketPair
import com.mini.ide.language.api.CompletionItem
import com.mini.ide.language.api.CompletionKind
import com.mini.ide.language.api.HighlightSpec
import com.mini.ide.language.api.LanguageId
import com.mini.ide.language.api.LanguageProfile
import com.mini.ide.language.api.Snippet
import com.mini.ide.language.api.SnippetScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageRegistryTest {

    @Test
    fun `内置注册HTML_CSS_JS三套Profile`() {
        val registry = LanguageRegistry()
        assertEquals(3, registry.all().size)
        assertEquals("text/html", registry.get(LanguageId.HTML).codemirrorMode)
        assertEquals("text/css", registry.get(LanguageId.CSS).codemirrorMode)
        assertEquals("text/javascript", registry.get(LanguageId.JS).codemirrorMode)
    }

    @Test
    fun `forExtension_按扩展名匹配语言_大小写不敏感`() {
        val registry = LanguageRegistry()
        assertEquals(LanguageId.HTML, registry.forExtension("HTML")!!.id)
        assertEquals(LanguageId.CSS, registry.forExtension("css")!!.id)
        assertEquals(LanguageId.JS, registry.forExtension("mjs")!!.id)
        assertNull(registry.forExtension("xyz"))
    }

    @Test
    fun `Phase3可插拔性_注册自定义DSL后编辑器零改动可用`() {
        val registry = LanguageRegistry()
        val dsl = object : LanguageProfile {
            override val id = LanguageId.DSL
            override val displayName = "MiniMe DSL"
            override val fileExtensions = listOf("mini")
            override val codemirrorMode = "text/x-minime"
            override val highlight = HighlightSpec(keywords = setOf("let", "show"))
            override val completions = listOf(CompletionItem("show", CompletionKind.KEYWORD, "show(\$0)"))
            override val snippets = listOf(Snippet("s", "show(\$0)", "显示", SnippetScope.JS))
            override val brackets = listOf(BracketPair('(', ')'))
            override val autoCloseChars = setOf('"')
        }
        registry.register(dsl)
        assertEquals(4, registry.all().size)
        assertEquals("MiniMe DSL", registry.get(LanguageId.DSL).displayName)
        assertEquals(LanguageId.DSL, registry.forExtension("mini")!!.id)
    }

    @Test
    fun `内置Profile含教学向补全与括号配置`() {
        assertTrue(HtmlProfile.completions.any { it.label == "div" })
        assertTrue(CssProfile.completions.any { it.label == "color" })
        assertTrue(JsProfile.completions.any { it.label == "console.log" })
        assertTrue(JsProfile.brackets.isNotEmpty())
    }
}
