package com.mini.ide.core.log

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** 时钟抽象，便于纯 JVM 单测控制日期。 */
interface LogClock {
    fun now(): ZonedDateTime
    fun date(): LocalDate = now().toLocalDate()
}

/** 真实时钟：固定 UTC+8（CONVENTIONS §3.1）。 */
class SystemLogClock : LogClock {
    override fun now(): ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC+08:00"))
}

/** 时间戳格式化：yyyy-MM-dd'T'HH:mm:ss+08:00。 */
object LogTimestamps {
    private val FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
    fun format(now: ZonedDateTime): String = now.format(FMT)
    fun fileDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}
