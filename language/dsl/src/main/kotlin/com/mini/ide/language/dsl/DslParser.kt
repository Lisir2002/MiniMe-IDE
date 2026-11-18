package com.mini.ide.language.dsl

import com.mini.ide.language.api.CompileDiagnostic
import com.mini.ide.language.api.CompileSeverity

/**
 * MiniLang 递归下降解析器。
 *
 * 设计要点（教学向宽容策略）：
 * - 逐行解析，遇到无法识别的语句只记一条 WARNING/ERROR 诊断并跳过该行，**不中断全文**；
 * - 关键字为闭集，未知词形不会崩溃；
 * - 卡片 `{}` 块递归解析子语句，深度做上限保护，防止病态输入导致栈溢出；
 * - 每个诊断都携带真实行号。
 */
class DslParser(private val maxDepth: Int = 32) {

    private val keywords = setOf("页面", "文字", "标题", "按钮", "卡片", "当点击", "显示", "设置", "背景", "颜色", "大小")
    private val elementWords = mapOf(
        "文字" to ElementType.TEXT,
        "标题" to ElementType.HEADING,
        "按钮" to ElementType.BUTTON,
        "卡片" to ElementType.CARD,
    )
    private val colorWords = setOf("红", "绿", "蓝", "深蓝", "浅灰", "白", "黑", "蓝")

    data class ParseResult(
        val statements: List<DslStatement>,
        val diagnostics: List<CompileDiagnostic>,
    )

    fun parse(source: String): ParseResult {
        val tokens = DslLexer.tokenize(source)
        val diagnostics = mutableListOf<CompileDiagnostic>()
        tokens.filter { it.type == DslTokenType.UNCLOSED_STRING }.forEach {
            diagnostics += CompileDiagnostic(it.line, "字符串缺少结束引号", CompileSeverity.ERROR)
        }
        val statements = parseBlock(tokens, 0, 0, diagnostics).statements
        return ParseResult(statements, diagnostics)
    }

    private data class BlockResult(val statements: List<DslStatement>, val next: Int)

    /** 解析到 RBRACE 或 EOF 为止的一段语句序列。 */
    private fun parseBlock(tokens: List<DslToken>, start: Int, depth: Int, diagnostics: MutableList<CompileDiagnostic>): BlockResult {
        if (depth > maxDepth) {
            diagnostics += CompileDiagnostic(tokens.getOrNull(start)?.line ?: 0, "嵌套层级过深，已跳过", CompileSeverity.ERROR)
            return BlockResult(emptyList(), start + 1)
        }
        val statements = mutableListOf<DslStatement>()
        var i = start
        while (i < tokens.size) {
            when (tokens[i].type) {
                DslTokenType.EOF, DslTokenType.RBRACE -> break
                DslTokenType.NEWLINE -> { i++; continue }
                else -> {
                    val parsed = parseStatement(tokens, i, depth, diagnostics)
                    parsed.statement?.let { statements += it }
                    i = parsed.next
                }
            }
        }
        return BlockResult(statements, i)
    }

    private data class StatementResult(val statement: DslStatement?, val next: Int)

    private fun parseStatement(
        tokens: List<DslToken>,
        start: Int,
        depth: Int,
        diagnostics: MutableList<CompileDiagnostic>,
    ): StatementResult {
        val head = tokens[start]
        // 跳过本语句之后到换行/块结束之间的 token（行导向：一行一语句）
        fun skipToLineEnd(from: Int): Int {
            var j = from
            while (j < tokens.size && tokens[j].type !in setOf(DslTokenType.NEWLINE, DslTokenType.EOF, DslTokenType.RBRACE)) j++
            return j
        }
        if (head.type != DslTokenType.WORD || head.value !in keywords) {
            diagnostics += CompileDiagnostic(head.line, "无法识别的语句「${head.value}」", CompileSeverity.ERROR)
            return StatementResult(null, skipToLineEnd(start + 1))
        }

        return when (head.value) {
            "页面" -> {
                val title = expectString(tokens, start + 1, head.line, "页面", diagnostics)
                StatementResult(title?.let { PageStatement(it, head.line) }, skipToLineEnd(start + 1 + (if (title != null) 1 else 0)))
            }
            in elementWords -> {
                val type = elementWords.getValue(head.value)
                val text = expectString(tokens, start + 1, head.line, head.value, diagnostics)
                var cursor = start + 1 + (if (text != null) 1 else 0)
                val attrs = mutableMapOf<String, String>()
                // 属性：`颜色 "值"` / `大小 "值"`，可重复、可缺省
                while (cursor < tokens.size && tokens[cursor].type == DslTokenType.WORD && tokens[cursor].value in setOf("颜色", "大小")) {
                    val key = tokens[cursor].value
                    val value = expectString(tokens, cursor + 1, tokens[cursor].line, key, diagnostics)
                    if (value != null) {
                        if (key == "颜色" && value !in colorWords && !isHexColor(value)) {
                            diagnostics += CompileDiagnostic(tokens[cursor].line, "未知颜色「$value」，将原样输出", CompileSeverity.WARNING)
                        }
                        attrs[key] = value
                        cursor += 2
                    } else {
                        cursor += 1
                    }
                }
                // 卡片块：`卡片 "…" { … }`
                var children = emptyList<DslStatement>()
                if (type == ElementType.CARD && cursor < tokens.size && tokens[cursor].type == DslTokenType.LBRACE) {
                    val block = parseBlock(tokens, cursor + 1, depth + 1, diagnostics)
                    children = block.statements
                    cursor = if (block.next < tokens.size && tokens[block.next].type == DslTokenType.RBRACE) block.next + 1 else block.next
                }
                val stmt = if (text != null) ElementStatement(type, text, attrs.toMap(), children, head.line) else null
                StatementResult(stmt, skipToLineEnd(cursor))
            }
            "当点击" -> {
                val buttonText = expectString(tokens, start + 1, head.line, "当点击", diagnostics)
                var cursor = start + 1 + (if (buttonText != null) 1 else 0)
                if (cursor < tokens.size && tokens[cursor].type == DslTokenType.WORD && tokens[cursor].value == "显示") cursor++
                else diagnostics += CompileDiagnostic(head.line, "点击事件缺少「显示」关键字", CompileSeverity.ERROR)
                val message = expectString(tokens, cursor, head.line, "显示", diagnostics)
                val stmt = if (buttonText != null && message != null) ClickStatement(buttonText, message, head.line) else null
                StatementResult(stmt, skipToLineEnd(cursor + (if (message != null) 1 else 0)))
            }
            "设置" -> {
                var cursor = start + 1
                val key = if (cursor < tokens.size && tokens[cursor].type == DslTokenType.WORD && tokens[cursor].value == "背景") {
                    cursor++
                    "背景"
                } else {
                    diagnostics += CompileDiagnostic(head.line, "设置语句目前仅支持「背景」", CompileSeverity.WARNING)
                    "未知"
                }
                val value = expectString(tokens, cursor, head.line, "设置", diagnostics)
                val stmt = if (value != null) SettingStatement(key, value, head.line) else null
                StatementResult(stmt, skipToLineEnd(cursor + (if (value != null) 1 else 0)))
            }
            else -> StatementResult(null, skipToLineEnd(start + 1))
        }
    }

    private fun expectString(
        tokens: List<DslToken>,
        index: Int,
        line: Int,
        owner: String,
        diagnostics: MutableList<CompileDiagnostic>,
    ): String? {
        val token = tokens.getOrNull(index)
        return when (token?.type) {
            DslTokenType.STRING -> token.value
            DslTokenType.UNCLOSED_STRING -> {
                diagnostics += CompileDiagnostic(line, "$owner 的字符串缺少结束引号", CompileSeverity.ERROR)
                null
            }
            else -> {
                diagnostics += CompileDiagnostic(line, "$owner 缺少引号包裹的文本参数", CompileSeverity.ERROR)
                null
            }
        }
    }

    private fun isHexColor(value: String): Boolean =
        value.startsWith("#") && (value.length == 4 || value.length == 7) && value.drop(1).all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
}
