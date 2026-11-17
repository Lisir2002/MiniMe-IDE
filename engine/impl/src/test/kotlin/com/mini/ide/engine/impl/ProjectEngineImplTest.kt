package com.mini.ide.engine.impl

import com.mini.ide.core.model.FileKind
import com.mini.ide.core.model.Project
import com.mini.ide.core.model.SourceFile
import com.mini.ide.testdouble.FakeFileRepository
import com.mini.ide.testdouble.FakeProjectRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectEngineImplTest {

    private fun sampleFiles() = listOf(
        SourceFile("f-html", "p1", "index.html", FileKind.HTML, "<h1>hi</h1>"),
        SourceFile("f-css", "p1", "style.css", FileKind.CSS, "h1{}"),
        SourceFile("f-js", "p1", "main.js", FileKind.JS, "var a=1"),
    )

    @Test
    fun `open_加载项目与文件并建立单一真相`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val projectRepo = FakeProjectRepository(listOf(Project("p1", "Demo")))
        val fileRepo = FakeFileRepository(sampleFiles())
        val engine = ProjectEngineImpl(projectRepo, fileRepo, dispatcher)

        val flow = engine.open("p1")
        advanceUntilIdle()
        val state = flow.first()
        assertEquals("p1", state.project.id)
        assertEquals(3, state.files.size)
        assertEquals("f-html", state.activeFileId) // 默认第一个文件
        assertTrue(!state.isDirty)
        engine.close()
    }

    @Test
    fun `markContentDirty_标记脏并同步内存镜像`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val projectRepo = FakeProjectRepository(listOf(Project("p1", "Demo")))
        val fileRepo = FakeFileRepository(sampleFiles())
        val engine = ProjectEngineImpl(projectRepo, fileRepo, dispatcher)
        engine.open("p1")
        advanceUntilIdle()

        engine.markContentDirty("f-html", "<h1>edited</h1>")
        val state = engine.observeActive().first()!!
        assertTrue(state.isDirty)
        assertEquals("<h1>edited</h1>", state.files.first { it.id == "f-html" }.content)
        engine.close()
    }

    @Test
    fun `saveActive_写回脏内容并清除脏标记`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val projectRepo = FakeProjectRepository(listOf(Project("p1", "Demo")))
        val fileRepo = FakeFileRepository(sampleFiles())
        val engine = ProjectEngineImpl(projectRepo, fileRepo, dispatcher)
        engine.open("p1")
        advanceUntilIdle()

        engine.markContentDirty("f-html", "<h1>saved</h1>")
        engine.saveActive()
        assertEquals(1, fileRepo.updated.size)
        assertEquals("f-html" to "<h1>saved</h1>", fileRepo.updated[0])
        assertTrue(!engine.observeActive().first()!!.isDirty)
        engine.close()
    }

    @Test
    fun `switchFile_切换活跃文件`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val engine = ProjectEngineImpl(
            FakeProjectRepository(listOf(Project("p1", "Demo"))),
            FakeFileRepository(sampleFiles()),
            dispatcher,
        )
        engine.open("p1")
        advanceUntilIdle()
        engine.switchFile("f-js")
        assertEquals("f-js", engine.observeActive().first()!!.activeFileId)
        engine.close()
    }

    @Test
    fun `currentPreviewDocument_从内存镜像拼三文件`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val engine = ProjectEngineImpl(
            FakeProjectRepository(listOf(Project("p1", "Demo"))),
            FakeFileRepository(sampleFiles()),
            dispatcher,
        )
        engine.open("p1")
        advanceUntilIdle()
        engine.markContentDirty("f-css", "body{color:blue}")

        val doc = engine.currentPreviewDocument("http://preview/")!!
        assertTrue(doc.html.contains("<h1>hi</h1>"))
        assertTrue(doc.html.contains("body{color:blue}"))
        assertTrue(doc.html.contains("var a=1"))
        assertEquals("http://preview/", doc.baseUrl)
        engine.close()
    }

    @Test
    fun `未打开项目时_previewDocument为null`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val engine = ProjectEngineImpl(
            FakeProjectRepository(), FakeFileRepository(), dispatcher,
        )
        assertEquals(null, engine.currentPreviewDocument("base"))
        engine.close()
    }
}
