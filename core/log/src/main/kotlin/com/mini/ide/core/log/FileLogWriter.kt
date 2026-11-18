package com.mini.ide.core.log

import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 文件日志写入器：按日分片 + 大小轮转 + 过期清理。
 * 仅依赖 JDK File，纯 JVM 可测（用临时目录 + FakeLogClock）。
 *
 * 目录结构：
 *   <dir>/minimide-YYYY-MM-DD.log       当日主文件
 *   <dir>/minimide-YYYY-MM-DD.log.1..5  超限滚动副本（.1 最新）
 */
class FileLogWriter(
    private val directory: File,
    private val clock: LogClock = SystemLogClock(),
    private val maxBytes: Long = DEFAULT_MAX_BYTES,
    private val maxBackups: Int = DEFAULT_MAX_BACKUPS,
    private val retainDays: Int = DEFAULT_RETAIN_DAYS,
) {

    /** 写入一条记录（含堆栈）。失败静默返回 false，日志系统绝不拖垮宿主。 */
    @Synchronized
    fun write(record: LogRecord): Boolean = try {
        prepareDirectory()
        cleanupExpired()
        val target = currentFile()
        rotateIfNeeded(target)
        target.appendText(LogFormatter.formatWithStack(record) + "\n")
        true
    } catch (_: IOException) {
        false
    }

    /** 当日主文件。 */
    fun currentFile(): File =
        File(directory, "minimide-${LogTimestamps.fileDate(clock.date())}.log")

    private fun prepareDirectory() {
        if (!directory.exists() && !directory.mkdirs() && !directory.exists()) {
            throw IOException("无法创建日志目录: ${directory.absolutePath}")
        }
    }

    /** 主文件超过上限时滚动：.5 删除，.4→.5 ... .1→.2，主文件→.1。 */
    private fun rotateIfNeeded(target: File) {
        if (!target.exists() || target.length() < maxBytes) return
        val oldest = File(directory, "${target.name}.$maxBackups")
        if (oldest.exists()) oldest.delete()
        for (i in (maxBackups - 1) downTo 1) {
            val from = File(directory, "${target.name}.$i")
            if (from.exists()) from.renameTo(File(directory, "${target.name}.${i + 1}"))
        }
        target.renameTo(File(directory, "${target.name}.1"))
    }

    /** 删除超过 retainDays 的日志分片（含 .N 副本），尊重用户存储。 */
    private fun cleanupExpired() {
        val today = clock.date()
        directory.listFiles { f -> f.name.startsWith(PREFIX) && f.isFile }
            ?.forEach { file ->
                val datePart = file.name.removePrefix(PREFIX).substringBefore(".").substringBefore(".log")
                runCatching { LocalDate.parse(datePart) }
                    .getOrNull()
                    ?.let { fileDate ->
                        if (ChronoUnit.DAYS.between(fileDate, today) > retainDays) file.delete()
                    }
            }
    }

    companion object {
        const val DEFAULT_MAX_BYTES = 1024L * 1024L
        const val DEFAULT_MAX_BACKUPS = 5
        const val DEFAULT_RETAIN_DAYS = 7
        const val PREFIX = "minimide-"
    }
}
