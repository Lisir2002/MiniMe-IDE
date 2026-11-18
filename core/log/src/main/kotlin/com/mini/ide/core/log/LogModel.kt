package com.mini.ide.core.log

/**
 * 日志级别，数值越大越严重。对齐 CONVENTIONS §3.2。
 * FATAL 用于未捕获异常（崩溃），ERROR 用于失败，WARN 可恢复，INFO 正常节点，DEBUG 排障。
 */
enum class LogLevel(val weight: Int, val label: String) {
    VERBOSE(0, "VERBOSE"),
    DEBUG(1, "DEBUG"),
    INFO(2, "INFO"),
    WARN(3, "WARN"),
    ERROR(4, "ERROR"),
    FATAL(5, "FATAL");

    /** 级别标签补齐到 7 字符左对齐（CONVENTIONS §3.1）。 */
    fun paddedLabel(): String = label.padEnd(7)
}

/**
 * 一条结构化日志记录。
 * @param timestamp ISO-8601 带时区字符串（如 2026-11-18T21:26:56+08:00）
 * @param level 级别
 * @param module 模块标签（gate/build/test/db/engine/bridge/ui/ci/...）
 * @param message 简洁描述
 * @param fields 结构化 key=value 数据；值含空格时调用方应自行加引号
 * @param throwable 可选堆栈（FATAL/ERROR 携带）
 */
data class LogRecord(
    val timestamp: String,
    val level: LogLevel,
    val module: String,
    val message: String,
    val fields: Map<String, String> = emptyMap(),
    val throwable: Throwable? = null,
)
