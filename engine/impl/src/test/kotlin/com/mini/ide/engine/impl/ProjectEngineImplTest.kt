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
import com.mini.ide.language.api.CompileDiagnostic
import com.mini.ide.language.api.CompileResult
import com.mini.ide.language.api.CompileSeverity
import com.mini.ide.testdouble.FakeDslCompiler
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

    @Test
    fun `M6含DSL文件时优先使用编译产物预览`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dslFiles = listOf(
            SourceFile("f-html", "p1", "index.html", FileKind.HTML, "<h1>手写</h1>"),
            SourceFile("f-dsl", "p1", "main.dsl", FileKind.DSL, "页面 \"编译\""),
        )
        val compiler = FakeDslCompiler(
            CompileResult("<!DOCTYPE html><html><head></head><body><h1>编译产物</h1></body></html>", "body{}", "", emptyList())
        )
        val engine = ProjectEngineImpl(
            FakeProjectRepository(listOf(Project("p1", "Demo"))),
            FakeFileRepository(dslFiles),
            dispatcher,
            dslCompiler = compiler,
        )
        engine.open("p1")
        advanceUntilIdle()
        val doc = engine.currentPreviewDocument("http://preview/")!!
        assertTrue(doc.html.contains("编译产物"))
        assertTrue(!doc.html.contains("手写"))
        assertEquals(1, compiler.compiled.size)
        assertEquals("页面 \"编译\"", compiler.compiled[0])
        engine.close()
    }

    @Test
    fun `M6无DSL文件时维持三文件逻辑且编译器不被调用`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val compiler = FakeDslCompiler()
        val engine = ProjectEngineImpl(
            FakeProjectRepository(listOf(Project("p1", "Demo"))),
            FakeFileRepository(sampleFiles()),
            dispatcher,
            dslCompiler = compiler,
        )
        engine.open("p1")
        advanceUntilIdle()
        val doc = engine.currentPreviewDocument("http://preview/")!!
        assertTrue(doc.html.contains("<h1>hi</h1>"))
        assertTrue(compiler.compiled.isEmpty())
        engine.close()
    }

    @Test
    fun `M6编译错误诊断透传到预览JS`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dslFiles = listOf(SourceFile("f-dsl", "p1", "main.dsl", FileKind.DSL, "???"))
        val compiler = FakeDslCompiler(
            CompileResult(
                "<html><head></head><body></body></html>", "", "",
                listOf(CompileDiagnostic(2, "无法识别", CompileSeverity.ERROR)),
            )
        )
        val engine = ProjectEngineImpl(
            FakeProjectRepository(listOf(Project("p1", "Demo"))),
            FakeFileRepository(dslFiles),
            dispatcher,
            dslCompiler = compiler,
        )
        engine.open("p1")
        advanceUntilIdle()
        val doc = engine.currentPreviewDocument("http://preview/")!!
        assertTrue(doc.js.contains("console.error"))
        assertTrue(doc.js.contains("第2行"))
        assertTrue(doc.js.contains("无法识别"))
        engine.close()
    }
}
