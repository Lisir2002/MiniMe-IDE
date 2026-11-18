package com.mini.ide.extension.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionRegistryTest {

    private class FakeLanguageProvider(private val ids: List<String>) : LanguageProvider {
        override fun languageIds(): List<String> = ids
    }

    @Test
    fun `注册后按类型取回扩展点`() {
        val registry = ExtensionRegistry()
        registry.register(FakeLanguageProvider(listOf("DSL", "HTML")))
        val providers = registry.all<LanguageProvider>()
        assertEquals(1, providers.size)
        assertEquals(listOf("DSL", "HTML"), providers.first().languageIds())
    }

    @Test
    fun `按类型过滤只返回目标类型`() {
        val registry = ExtensionRegistry()
        registry.register(FakeLanguageProvider(listOf("DSL")))
        assertTrue(registry.all<ShareHandler>().isEmpty())
        assertEquals(1, registry.all<LanguageProvider>().size)
    }

    @Test
    fun `同名类型覆盖与clear`() {
        val registry = ExtensionRegistry()
        registry.register(FakeLanguageProvider(listOf("A")))
        registry.register(FakeLanguageProvider(listOf("B")))
        assertEquals(1, registry.size)
        assertEquals(listOf("B"), registry.all<LanguageProvider>().first().languageIds())
        registry.clear()
        assertEquals(0, registry.size)
    }
}
