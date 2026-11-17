package com.mini.ide.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** M0 契约测试：验证共享数据模型的默认行为与值语义。 */
class ModelsTest {

    @Test
    fun `SourceFile 默认内容为空字符串`() {
        val file = SourceFile(
            id = "f1",
            projectId = "p1",
            name = "index.html",
            kind = FileKind.HTML,
        )
        assertEquals("", file.content)
    }

    @Test
    fun `Project 的 templateId 默认为 null`() {
        val project = Project(id = "p1", name = "Demo")
        assertNull(project.templateId)
    }

    @Test
    fun `data class copy 只覆盖指定字段`() {
        val file = SourceFile("f1", "p1", "style.css", FileKind.CSS, "body{}")
        val renamed = file.copy(name = "main.css")
        assertEquals("main.css", renamed.name)
        assertEquals("body{}", renamed.content)
        assertEquals("f1", renamed.id)
    }

    @Test
    fun `数据类遵循值相等语义`() {
        val a = Project("p1", "Demo")
        val b = Project("p1", "Demo")
        val c = Project("p2", "Demo")
        assertEquals(a, b)
        assertNotEquals(a, c)
    }

    @Test
    fun `文件类型枚举覆盖教学三件套与 DSL 预留`() {
        assertEquals(4, FileKind.entries.size)
        assertEquals(
            setOf(FileKind.HTML, FileKind.CSS, FileKind.JS, FileKind.DSL),
            FileKind.entries.toSet(),
        )
    }
}
