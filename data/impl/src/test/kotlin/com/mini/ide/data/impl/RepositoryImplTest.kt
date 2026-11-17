package com.mini.ide.data.impl

import androidx.test.core.app.ApplicationProvider
import com.mini.ide.core.model.FileKind
import com.mini.ide.core.model.SourceFile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * M1 数据系统本地单测（Robolectric + Room 内存库，纯 JVM 运行，无需真机/云端）。
 * 覆盖项目与文件仓库的 CRUD、模板实例化、内容更新、级联删除。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RepositoryImplTest {

    private lateinit var db: MiniMeDatabase
    private lateinit var templates: TemplateRepositoryImpl
    private lateinit var projects: ProjectRepositoryImpl
    private lateinit var files: FileRepositoryImpl
    private lateinit var contentStore: ContentStore

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = MiniMeDatabase.buildInMemory(context)
        contentStore = DbContentStore(db.fileDao())
        templates = TemplateRepositoryImpl(db)
        projects = ProjectRepositoryImpl(db, templates)
        files = FileRepositoryImpl(db, contentStore)
        runBlocking { templates.ensureSeeded() }
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `内置模板注入幂等且包含空白与示例`() = runBlocking {
        val all = templates.all()
        assertEquals(2, all.size)
        assertTrue(all.any { it.id == "blank" })
        assertTrue(all.any { it.id == "hello" })
        // 再次 seed 不应重复
        templates.ensureSeeded()
        assertEquals(2, templates.all().size)
    }

    @Test
    fun `创建项目并观察列表`() = runBlocking {
        val p = projects.create("我的网页")
        assertNotNull(projects.get(p.id))
        assertEquals("我的网页", projects.get(p.id)!!.name)
        assertEquals(1, projects.observeAll().first().size)
    }

    @Test
    fun `基于模板创建项目会实例化三个文件`() = runBlocking {
        val p = projects.create("示例", templateId = "hello")
        val list = files.observeByProject(p.id).first()
        assertEquals(3, list.size)
        assertEquals(FileKind.HTML, list.first { it.name == "index.html" }.kind)
        assertTrue(list.first { it.name == "main.js" }.content.contains("addEventListener"))
    }

    @Test
    fun `更新文件内容后读取为新值`() = runBlocking {
        val p = projects.create("编辑测试", templateId = "blank")
        val file = files.observeByProject(p.id).first().first { it.name == "style.css" }
        files.updateContent(file.id, "body { color: red; }")
        assertEquals("body { color: red; }", files.get(file.id)!!.content)
    }

    @Test
    fun `ContentStore 读写与仓库一致`() = runBlocking {
        val p = projects.create("Store 测试", templateId = "blank")
        val file = files.observeByProject(p.id).first().first { it.name == "main.js" }
        contentStore.write(file.id, "console.log('x')")
        assertEquals("console.log('x')", contentStore.read(file.id))
    }

    @Test
    fun `重命名与删除项目`() = runBlocking {
        val p = projects.create("待改名")
        projects.rename(p.id, "已改名")
        assertEquals("已改名", projects.get(p.id)!!.name)
        projects.delete(p.id)
        assertNull(projects.get(p.id))
    }

    @Test
    fun `删除项目级联删除其文件`() = runBlocking {
        val p = projects.create("级联", templateId = "hello")
        assertEquals(3, files.observeByProject(p.id).first().size)
        projects.delete(p.id)
        assertTrue(files.observeByProject(p.id).first().isEmpty())
    }

    @Test
    fun `upsertAll 批量写入文件`() = runBlocking {
        val p = projects.create("批量")
        val batch = listOf(
            SourceFile("sf1", p.id, "a.js", FileKind.JS, "a", 0),
            SourceFile("sf2", p.id, "b.js", FileKind.JS, "b", 1),
        )
        files.upsertAll(batch)
        assertEquals(2, files.observeByProject(p.id).first().size)
    }
}
