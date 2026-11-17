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
        }
        assertEquals(LanguageId.DSL, custom.id)
        assertEquals("MiniMe DSL", custom.displayName)
    }
}
