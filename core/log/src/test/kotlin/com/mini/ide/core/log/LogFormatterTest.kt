package com.mini.ide.core.log

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class FakeLogClock(private val date: LocalDate) : LogClock {
    override fun now(): ZonedDateTime = date.atTime(21, 26, 56).atZone(ZoneId.of("UTC+08:00"))
    override fun date(): LocalDate = date
}

class LogFormatterTest {

    private fun record(
        level: LogLevel = LogLevel.INFO,
        module: String = "project",
        message: String = "新建项目成功",
        fields: Map<String, String> = mapOf("id" to "uuid-1", "count" to "3"),
        throwable: Throwable? = null,
    ) = LogRecord(
        timestamp = "2026-11-18T21:26:56+08:00",
        level = level, module = module, message = message,
        fields = fields, throwable = throwable,
    )

    @Test
    fun `格式字段顺序与补齐正确`() {
        val out = LogFormatter.format(record())
        assertEquals(
            "[2026-11-18T21:26:56+08:00] [INFO   ] [project ] 新建项目成功 id=uuid-1 count=3",
            out,
        )
    }

    @Test
    fun `级别标签补齐到7字符`() {
        assertTrue(LogFormatter.format(record(level = LogLevel.WARN)).contains("[WARN   ]"))
        assertTrue(LogFormatter.format(record(level = LogLevel.FATAL)).contains("[FATAL  ]"))
    }

    @Test
    fun `模块标签补齐到8字符`() {
        assertTrue(LogFormatter.format(record(module = "ci")).contains("[ci      ]"))
    }

    @Test
    fun `敏感字段值被脱敏`() {
        val out = LogFormatter.format(record(fields = mapOf("token" to "ghp_abcdefgh12345678", "user" to "alice")))
        assertTrue(out.contains("token=<hidden>"))
        assertTrue(out.contains("user=alice"))
        assertTrue(!out.contains("ghp_abcdefgh"))
    }

    @Test
    fun `消息中的token前缀被脱敏`() {
        val out = LogFormatter.format(record(message = "auth ghp_abcdefgh12345678 done"))
        assertTrue(out.contains("<hidden>"))
        assertTrue(!out.contains("ghp_abcdefgh"))
    }

    @Test
    fun `堆栈附加在消息之后`() {
        val t = IllegalStateException("boom")
        val out = LogFormatter.formatWithStack(record(level = LogLevel.FATAL, throwable = t))
        assertTrue(out.startsWith("[2026-11-18T21:26:56+08:00]"))
        assertTrue(out.contains("IllegalStateException: boom"))
        assertTrue(out.contains("\tat "))
    }

    @Test
    fun `时间戳格式正确`() {
        val ts = LogTimestamps.format(FakeLogClock(LocalDate.of(2026, 11, 18)).now())
        assertEquals("2026-11-18T21:26:56+08:00", ts)
    }
}
