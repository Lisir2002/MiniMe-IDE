package com.mini.ide.feature.editor

import com.mini.ide.core.common.DispatcherProvider
import com.mini.ide.core.model.Project
import com.mini.ide.core.model.SourceFile
import com.mini.ide.core.model.FileKind
import com.mini.ide.engine.api.EditorEvent
import com.mini.ide.engine.api.ProjectState
import com.mini.ide.testdouble.FakeBridgeEngine
import com.mini.ide.testdouble.FakeEditorEngine
import com.mini.ide.testdouble.FakeLanguageProvider
import com.mini.ide.testdouble.FakeProjectEngine
import com.mini.ide.testdouble.FakeWebViewPort
import com.mini.ide.testdouble.FakeSettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = testDispatcher
        override val io: CoroutineDispatcher = testDispatcher
        override val default: CoroutineDispatcher = testDispatcher
    }

    private fun projectState(activeId: String) = ProjectState(
        project = Project("p1", "测试项目"),
        files = listOf(
            SourceFile("f-html", "p1", "index.html", FileKind.HTML, "<h1>hi</h1>", 0),
            SourceFile("f-css", "p1", "style.css", FileKind.CSS, "body{}", 1),
        ),
        activeFileId = activeId,
        isDirty = false,
    )

    private fun createVm(
        projectEngine: FakeProjectEngine,
        editorEngine: FakeEditorEngine,
        bridge: FakeBridgeEngine,
    ) = EditorViewModel(
        projectEngine, editorEngine, bridge, editorEngine, FakeLanguageProvider(),
        FakeSettingsRepository(), dispatchers,
    )

    @Test
    fun `open loads active file into editor`() = runTest(testDispatcher) {
        val projectEngine = FakeProjectEngine().apply { pushState(projectState("f-html")) }
        val editorEngine = FakeEditorEngine()
        val vm = createVm(projectEngine, editorEngine, FakeBridgeEngine())
        vm.attach(FakeWebViewPort())
        vm.open("p1")
        advanceUntilIdle()
        assertEquals(1, editorEngine.loaded.size)
        assertEquals("f-html", editorEngine.loaded.first().first)
        assertEquals("<h1>hi</h1>", editorEngine.loaded.first().second)
        assertEquals("测试项目", vm.uiState.value.projectName)
    }

    @Test
    fun `content changed event marks dirty`() = runTest(testDispatcher) {
        val projectEngine = FakeProjectEngine().apply { pushState(projectState("f-html")) }
        val editorEngine = FakeEditorEngine()
        val vm = createVm(projectEngine, editorEngine, FakeBridgeEngine())
        vm.attach(FakeWebViewPort())
        vm.open("p1")
        advanceUntilIdle()
        editorEngine.emit(EditorEvent.ContentChanged("f-html", "<h1>changed</h1>"))
        advanceUntilIdle()
        assertTrue(editorEngine.loaded.isNotEmpty())
        assertEquals("f-html", projectEngine.dirtyMarks.first().first)
        assertEquals("<h1>changed</h1>", projectEngine.dirtyMarks.first().second)
        assertTrue(vm.uiState.value.isDirty)
    }

    @Test
    fun `switch file loads target content`() = runTest(testDispatcher) {
        val projectEngine = FakeProjectEngine().apply { pushState(projectState("f-html")) }
        val editorEngine = FakeEditorEngine()
        val vm = createVm(projectEngine, editorEngine, FakeBridgeEngine())
        vm.attach(FakeWebViewPort())
        vm.open("p1")
        advanceUntilIdle()
        vm.switchFile("f-css")
        advanceUntilIdle()
        assertEquals("f-css", editorEngine.loaded.last().first)
        assertEquals("body{}", editorEngine.loaded.last().second)
        assertEquals("f-css", vm.uiState.value.activeFileId)
    }

    @Test
    fun `attach installs bridge with handler`() = runTest(testDispatcher) {
        val bridge = FakeBridgeEngine()
        val vm = createVm(FakeProjectEngine().apply { pushState(projectState("f-html")) }, FakeEditorEngine(), bridge)
        val port = FakeWebViewPort()
        vm.attach(port)
        assertTrue(bridge.handler != null)
    }
}
