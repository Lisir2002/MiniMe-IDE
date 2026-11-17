package com.mini.ide.language.api

/** 语言标识。DSL 为 Phase3 自研语言预留位。 */
enum class LanguageId { HTML, CSS, JS, DSL }

/** 教学向语法高亮规格：只描述关键词/注释/字符串等少量规则，非工业全量 grammar。 */
data class HighlightSpec(
    val keywords: Set<String> = emptySet(),
    val builtins: Set<String> = emptySet(),
    val commentPrefix: String? = null,
    val stringDelimiters: Set<Char> = emptySet(),
)

/** 补全候选项。 */
data class CompletionItem(
    val label: String,
    val kind: CompletionKind,
    val insertText: String,
    val detail: String? = null,
)

enum class CompletionKind { TAG, ATTRIBUTE, PROPERTY, VALUE, KEYWORD, SNIPPET }

/** 代码片段。 */
data class Snippet(
    val trigger: String,
    val expansion: String,
    val description: String,
    val scope: SnippetScope,
)

enum class SnippetScope { HTML, CSS, JS }

/** 括号配对，用于编辑器自动闭合。 */
data class BracketPair(val open: Char, val close: Char)

/**
 * 语言配置档案：数据驱动的可插拔单元。
 *
 * Phase3 自研语言 = 新增一个 Profile 实例并注册进 LanguageRegistry，编辑器内核零改动。
 */
interface LanguageProfile {
    val id: LanguageId
    val displayName: String
    val fileExtensions: List<String>
    val codemirrorMode: String
    val highlight: HighlightSpec
    val completions: List<CompletionItem>
    val snippets: List<Snippet>
    val brackets: List<BracketPair>
    val autoCloseChars: Set<Char>
}
