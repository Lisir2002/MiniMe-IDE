package com.mini.ide.language.api

import com.mini.ide.core.model.FileKind

/**
 * 语言提供方（只读端口）—— feature 层按文件类型取语言配置的契约。
 *
 * 动机：LanguageRegistry 是 language:impl 的具体类，而架构铁律禁止 feature 依赖 impl。
 * 故在 api 层暴露只读接口，impl 实现并由 Hilt 绑定。M4 集成期的最小契约补充，
 * 向后兼容 M2（不改 LanguageProfile，不动 M2 测试）。
 */
interface LanguageProvider {
    /** 按源码文件类型取语言配置；未知类型（如 DSL 未注册）回退到 HTML。 */
    fun forKind(kind: FileKind): LanguageProfile

    /** 全部已注册语言，供 UI 展示。 */
    fun all(): List<LanguageProfile>
}
