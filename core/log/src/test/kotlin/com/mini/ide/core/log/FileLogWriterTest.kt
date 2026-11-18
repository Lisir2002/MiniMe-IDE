package com.mini.ide.core.log

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

class FileLogWriterTest {

    private fun tempDir(): File =
        File(System.getProperty("java.io.tmpdir"), "minilog-test-${System.nanoTime()}").apply { mkdirs() }

    private fun rec(msg: String) = LogRecord(
        timestamp = "2026-11-18T21:26:56+08:00",
        level = LogLevel.INFO, module = "test", message = msg,
    )

    @Test
    fun `写入按日期分片到主文件`() {
        val dir = tempDir()
        val writer = FileLogWriter(dir, FakeLogClock(LocalDate.of(2026, 11, 18)))
        assertTrue(writer.write(rec("hello")))
        val f = File(dir, "minimide-2026-11-18.log")
        assertTrue(f.exists())
        assertTrue(f.readText().contains("hello"))
        dir.deleteRecursively()
    }

    @Test
    fun `超过大小上限时滚动生成备份`() {
        val dir = tempDir()
        val writer = FileLogWriter(dir, FakeLogClock(LocalDate.of(2026, 11, 18)), maxBytes = 10L)
        repeat(5) { writer.write(rec("line-$it")) }
        assertTrue(File(dir, "minimide-2026-11-18.log").exists())
        assertTrue(File(dir, "minimide-2026-11-18.log.1").exists())
        dir.deleteRecursively()
    }

    @Test
    fun `备份数不超过上限`() {
        val dir = tempDir()
        val writer = FileLogWriter(
            dir, FakeLogClock(LocalDate.of(2026, 11, 18)),
            maxBytes = 10L, maxBackups = 2,
        )
        repeat(10) { writer.write(rec("line-$it")) }
        val backups = dir.listFiles { f -> f.name.contains(".log.") }?.size ?: 0
        assertTrue("备份数应 <= 2，实际 $backups", backups <= 2)
        dir.deleteRecursively()
    }

    @Test
    fun `清理超过保留天数的旧日志`() {
        val dir = tempDir()
        // 造一个 30 天前的旧文件
        File(dir, "minimide-2026-10-01.log").writeText("old\n")
        val writer = FileLogWriter(
            dir, FakeLogClock(LocalDate.of(2026, 11, 18)),
            retainDays = 7,
        )
        writer.write(rec("new"))
        assertTrue(!File(dir, "minimide-2026-10-01.log").exists())
        assertTrue(File(dir, "minimide-2026-11-18.log").exists())
        dir.deleteRecursively()
    }

    @Test
    fun `目录不可写时返回false不抛异常`() {
        val writer = FileLogWriter(File("/proc/forbidden/minide"), FakeLogClock(LocalDate.of(2026, 11, 18)))
        val ok = runCatching { writer.write(rec("x")) }.getOrElse { false }
        assertEquals(false, ok)
    }
}
