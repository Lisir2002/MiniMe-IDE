package com.mini.ide.language.api

enum class LanguageId { HTML, CSS, JS, DSL }

interface LanguageProfile {
    val id: LanguageId
    val displayName: String
}
