package com.mini.ide.core.log

/**
 * 结构化日志格式化器（CONVENTIONS §3.1 唯一实现，纯函数可 JVM 单测）。
 *
 * 输出：
 * [2026-11-18T21:26:56+08:00] [INFO   ] [project ] 新建项目成功 id=uuid-xxx
 * [..] [FATAL  ] [crash   ] 未捕获异常 thread=main type=NullPointerException
 * java.lang.NullPointerException: ...
 *     at ...
 */
object LogFormatter {

    /** 模块标签补齐到 8 字符。 */
    private fun String.paddedModule(): String = take(8).padEnd(8)

    /** 敏感 key：命中则值替换为 <hidden>（CONVENTIONS §3.2 硬规则）。 */
    private val SENSITIVE_KEYS = setOf("token", "password", "secret", "key", "authorization", "apikey")
    private fun isSensitive(key: String): Boolean =
        SENSITIVE_KEYS.any { key.contains(it, ignoreCase = true) }

    /** 消息正文里如出现 ghp_/github_pat_ 等 token 前缀，做兜底脱敏。 */
    private fun String.redactSecrets(): String =
        replace(Regex("gh[pousr]_[A-Za-z0-9]{8,}"), "<hidden>")
            .replace(Regex("github_pat_[A-Za-z0-9_]{8,}"), "<hidden>")

    /** 格式化单条记录（不含堆栈）。 */
    fun format(record: LogRecord): String = buildString {
        append('[').append(record.timestamp).append("] ")
        append('[').append(record.level.paddedLabel()).append("] ")
        append('[').append(record.module.paddedModule()).append("] ")
        append(record.message.redactSecrets())
        record.fields.entries.forEach { (k, v) ->
            append(' ').append(k).append('=')
            append(if (isSensitive(k)) "<hidden>" else v.redactSecrets())
        }
    }

    /** 格式化含堆栈的完整记录（FATAL/ERROR）。 */
    fun formatWithStack(record: LogRecord): String = buildString {
        appendLine(format(record))
        record.throwable?.let { t ->
            append(t.stackTraceToString())
        }
    }
}
