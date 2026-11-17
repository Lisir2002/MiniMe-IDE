package com.mini.ide.language.impl

import com.mini.ide.language.api.LanguageId
import com.mini.ide.language.api.LanguageProfile

/**
 * 语言注册表：可插拔核心。
 *
 * 内置三语言在构造时注册；Phase3 自研 DSL 只需 register 一个新 Profile，
 * 编辑器内核与 UI 无需改动——这是"自研语言放最后"能成立的关键。
 */
class LanguageRegistry {
    private val profiles = mutableMapOf<LanguageId, LanguageProfile>()

    init {
        register(HtmlProfile)
        register(CssProfile)
        register(JsProfile)
    }

    fun register(profile: LanguageProfile) {
        profiles[profile.id] = profile
    }

    fun get(id: LanguageId): LanguageProfile =
        profiles[id] ?: throw NoSuchElementException("未注册语言: $id")

    fun forExtension(ext: String): LanguageProfile? =
        profiles.values.firstOrNull { ext.lowercase() in it.fileExtensions.map { e -> e.lowercase() } }

    fun all(): List<LanguageProfile> = profiles.values.toList()
}
