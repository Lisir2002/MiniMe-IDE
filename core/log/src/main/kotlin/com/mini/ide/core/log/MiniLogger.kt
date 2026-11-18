package com.mini.ide.core.log

import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 日志门面（CONVENTIONS §3 的 Kotlin 侧统一出口）。
 * 持有可替换 [FileLogWriter]；writer 未装配时仅降级到 android.util.Log，绝不崩溃。
 */
@Singleton
class MiniLogger @Inject constructor() {

    @Volatile
    private var writer: FileLogWriter? = null

    @Volatile
    private var minLevel: LogLevel = LogLevel.DEBUG

    /** 由 app 装配文件写入器（权限确定后调用）。 */
    fun attach(writer: FileLogWriter, minLevel: LogLevel = LogLevel.DEBUG) {
        this.writer = writer
        this.minLevel = minLevel
    }

    fun log(level: LogLevel, module: String, message: String, throwable: Throwable? = null, vararg fields: Pair<String, String>) {
        if (level.weight < minLevel.weight) return
        val now: ZonedDateTime = SystemLogClock().now()
        val record = LogRecord(
            timestamp = LogTimestamps.format(now),
            level = level,
            module = module,
            message = message,
            fields = fields.toMap(),
            throwable = throwable,
        )
        writer?.write(record)
        // 同时输出到 logcat（DEBUG 构建可见；release 由系统策略过滤）
        android.util.Log.println(
            when (level) {
                LogLevel.VERBOSE -> android.util.Log.VERBOSE
                LogLevel.DEBUG -> android.util.Log.DEBUG
                LogLevel.INFO -> android.util.Log.INFO
                LogLevel.WARN -> android.util.Log.WARN
                LogLevel.ERROR, LogLevel.FATAL -> android.util.Log.ERROR
            },
            "MiniMe/${module.take(8)}",
            LogFormatter.format(record),
        )
    }

    fun debug(module: String, message: String, vararg fields: Pair<String, String>) =
        log(LogLevel.DEBUG, module, message, null, *fields)
    fun info(module: String, message: String, vararg fields: Pair<String, String>) =
        log(LogLevel.INFO, module, message, null, *fields)
    fun warn(module: String, message: String, vararg fields: Pair<String, String>) =
        log(LogLevel.WARN, module, message, null, *fields)
    fun error(module: String, message: String, throwable: Throwable? = null, vararg fields: Pair<String, String>) =
        log(LogLevel.ERROR, module, message, throwable, *fields)
    fun fatal(module: String, message: String, throwable: Throwable? = null, vararg fields: Pair<String, String>) =
        log(LogLevel.FATAL, module, message, throwable, *fields)
}
