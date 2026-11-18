package com.mini.ide.language.dsl

/**
 * MiniLang 词法分析器：把源文本切成 token 流。纯 JVM，无正则引擎依赖。
 *
 * 词法规则（教学向、刻意精简）：
 * - 一行一条语句，换行产生 [DslTokenType.NEWLINE]；
 * - 以 `#` 开头到行尾为注释，整体丢弃；
 * - 双引号包裹字符串内容，支持 `\"` 与 `\\` 转义，字符串内容不再做关键字识别；
 * - `{` / `}` 作为块定界符；其余连续非空白字符组成一个 [DslTokenType.WORD]。
 *
 * 每个 token 携带行号，供编译诊断定位。
 */
object DslLexer {

    fun tokenize(source: String): List<DslToken> {
        val tokens = mutableListOf<DslToken>()
        var line = 1
        var i = 0
        while (i < source.length) {
            when (val c = source[i]) {
                '\n' -> {
                    tokens += DslToken(DslTokenType.NEWLINE, "\n", line)
                    line++
                    i++
                }
                '\r' -> i++ // 兼容 CRLF，不产生额外换行
                ' ', '\t' -> i++
                '#' -> { // 注释：吞到行尾（换行交给下一轮处理）
                    while (i < source.length && source[i] != '\n') i++
                }
                '{' -> { tokens += DslToken(DslTokenType.LBRACE, "{", line); i++ }
                '}' -> { tokens += DslToken(DslTokenType.RBRACE, "}", line); i++ }
                '"' -> {
                    val startLine = line
                    i++ // 跳过起始引号
                    val sb = StringBuilder()
                    var closed = false
                    while (i < source.length) {
                        when (source[i]) {
                            '\\' -> {
                                if (i + 1 < source.length) {
                                    when (source[i + 1]) {
                                        '"' -> { sb.append('"'); i += 2 }
                                        '\\' -> { sb.append('\\'); i += 2 }
                                        else -> { sb.append('\\'); i++ }
                                    }
                                } else {
                                    sb.append('\\'); i++
                                }
                            }
                            '"' -> { i++; closed = true; break }
                            '\n' -> { // 允许字符串跨行，行号推进
                                sb.append('\n'); line++; i++
                            }
                            else -> { sb.append(source[i]); i++ }
                        }
                    }
                    tokens += DslToken(
                        if (closed) DslTokenType.STRING else DslTokenType.UNCLOSED_STRING,
                        sb.toString(),
                        startLine,
                    )
                }
                else -> {
                    val start = i
                    while (i < source.length && !source[i].isWhitespace() && source[i] !in "\"{}#") i++
                    tokens += DslToken(DslTokenType.WORD, source.substring(start, i), line)
                }
            }
        }
        tokens += DslToken(DslTokenType.EOF, "", line)
        return tokens
    }
}

enum class DslTokenType {
    WORD, STRING, UNCLOSED_STRING, LBRACE, RBRACE, NEWLINE, EOF,
}

data class DslToken(val type: DslTokenType, val value: String, val line: Int)
