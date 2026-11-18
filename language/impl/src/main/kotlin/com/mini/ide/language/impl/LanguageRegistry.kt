package com.mini.ide.language.impl

import com.mini.ide.core.model.FileKind
import com.mini.ide.language.api.LanguageId
import com.mini.ide.language.api.LanguageProvider
import com.mini.ide.language.api.LanguageProfile
import com.mini.ide.language.dsl.DslProfile

/**
 * 语言注册表：可插拔核心。
 *
 * 内置三语言在构造时注册；Phase3 自研 DSL 只需 register 一个新 Profile，
 * 编辑器内核与 UI 无需改动——这是"自研语言放最后"能成立的关键。
 */
class LanguageRegistry : LanguageProvider {
    private val profiles = mutableMapOf<LanguageId, LanguageProfile>()

    init {
        register(HtmlProfile)
        register(CssProfile)
        register(JsProfile)
        register(DslProfile) // M6：自研语言注册，编辑器内核零改动即获得 MiniLang 配置
    }

    fun register(profile: LanguageProfile) {
        profiles[profile.id] = profile
    }

    fun get(id: LanguageId): LanguageProfile =
        profiles[id] ?: throw NoSuchElementException("未注册语言: $id")

    fun forExtension(ext: String): LanguageProfile? =
        profiles.values.firstOrNull { ext.lowercase() in it.fileExtensions.map { e -> e.lowercase() } }

    override fun all(): List<LanguageProfile> = profiles.values.toList()

    /** LanguageProvider：FileKind → LanguageProfile，未知类型回退 HTML。 */
    override fun forKind(kind: FileKind): LanguageProfile = when (kind) {
        FileKind.HTML -> get(LanguageId.HTML)
        FileKind.CSS -> get(LanguageId.CSS)
        FileKind.JS -> get(LanguageId.JS)
        FileKind.DSL -> profiles[LanguageId.DSL] ?: get(LanguageId.HTML)
    }
}
