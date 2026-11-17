package com.mini.ide.data.impl

import com.mini.ide.core.model.FileKind
import com.mini.ide.core.model.SourceFileDraft
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** M1：模板文件列表的 JSON 往返一致性（org.json 需 Robolectric 环境）。 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MappersTest {

    @Test
    fun `文件草稿列表 JSON 往返无损`() {
        val drafts = listOf(
            SourceFileDraft("index.html", FileKind.HTML, "<h1>hi</h1>\n", 0),
            SourceFileDraft("main.js", FileKind.JS, "const x = 'a\"b';\nconsole.log(x)", 1),
        )
        val json = Mappers.encodeFiles(drafts)
        val decoded = Mappers.decodeFiles(json)
        assertEquals(drafts, decoded)
    }

    @Test
    fun `未知 kind 回退为 HTML 保证健壮性`() {
        val decoded = Mappers.decodeFiles("""[{"name":"x.css","kind":"UNKNOWN","content":"","sortOrder":0}]""")
        assertEquals(FileKind.HTML, decoded.single().kind)
    }

    @Test
    fun `内置模板的 JSON 均可解码且文件数正确`() {
        BuiltinTemplates.all().forEach { t ->
            val files = Mappers.decodeFiles(Mappers.encodeFiles(t.files))
            assertEquals(t.id, t.files.size, files.size)
        }
    }
}
