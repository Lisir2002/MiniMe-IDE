package com.mini.ide.feature.preview

import com.mini.ide.core.common.DispatcherProvider
import com.mini.ide.core.model.Project
import com.mini.ide.core.model.Settings
import com.mini.ide.data.api.SettingsRepository
import com.mini.ide.engine.api.BridgeMessage
import com.mini.ide.engine.api.DiagnosticCategory
import com.mini.ide.engine.api.LogLevel
import com.mini.ide.engine.api.PreviewDocument
import com.mini.ide.engine.api.ProjectState
import com.mini.ide.testdouble.FakeDiagnosticEngine
import com.mini.ide.testdouble.FakeProjectEngine
import com.mini.ide.testdouble.FakeRenderEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PreviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = testDispatcher
        override val io: CoroutineDispatcher = testDispatcher
        override val default: CoroutineDispatcher = testDispatcher
    }
    private val settingsRepo = object : SettingsRepository {
        private val state = MutableStateFlow(Settings(previewDebounceMs = 100L))
        override fun observe(): Flow<Settings> = state
        override suspend fun update(transform: (Settings) -> Settings) { state.value = transform(state.value) }
    }
    private val doc = PreviewDocument("<html></html>", "", "", "base")
    private val state = ProjectState(Project("p1", "t"), emptyList(), "", false)

    private fun createVm(
        projectEngine: FakeProjectEngine,
        render: FakeRenderEngine,
        diagnostic: FakeDiagnosticEngine = FakeDiagnosticEngine(),
    ) = PreviewViewModel(projectEngine, render, settingsRepo, diagnostic, dispatchers)

    @Test
    fun `state change triggers debounced render`() = runTest(testDispatcher) {
        val pe = FakeProjectEngine().apply { previewDocument = doc }
        val re = FakeRenderEngine()
        val vm = createVm(pe, re)
        vm.open("p1")
        advanceUntilIdle() // 建立订阅
        pe.pushState(state) // 状态变化触发 debounce
        advanceTimeBy(200)
        advanceUntilIdle()
        assertTrue(re.rendered.isNotEmpty())
        // 桥接脚本已注入
        assertTrue(re.rendered.first().html.contains("AndroidBridge.post"))
    }

    @Test
    fun `run renders immediately`() = runTest(testDispatcher) {
        val pe = FakeProjectEngine().apply { previewDocument = doc; pushState(state) }
        val re = FakeRenderEngine()
        val vm = createVm(pe, re)
        vm.run()
        advanceUntilIdle()
        assertEquals(1, re.rendered.size)
    }

    @Test
    fun `bridge console message becomes log`() = runTest(testDispatcher) {
        val pe = FakeProjectEngine().apply { previewDocument = doc; pushState(state) }
        val re = FakeRenderEngine()
        val vm = createVm(pe, re)
        advanceUntilIdle()
        vm.onBridgeMessage(BridgeMessage.CONSOLE.name, "ERROR|12|boom")
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.logs.size)
        assertEquals("boom", vm.uiState.value.logs.first().message)
        assertEquals(12, vm.uiState.value.logs.first().line)
    }

    @Test
    fun `bridge runtime error becomes error log`() = runTest(testDispatcher) {
        val pe = FakeProjectEngine().apply { pushState(state) }
        val re = FakeRenderEngine()
        val vm = createVm(pe, re)
        advanceUntilIdle()
        vm.onBridgeMessage(BridgeMessage.RUNTIME_ERROR.name, "5|app.js|TypeError")
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.logs.size)
        assertEquals("TypeError", vm.uiState.value.logs.first().message)
        assertEquals("app.js", vm.uiState.value.logs.first().source)
    }

    @Test
    fun `运行时错误经诊断引擎升级为结构化日志`() = runTest(testDispatcher) {
        val pe = FakeProjectEngine().apply { pushState(state) }
        val re = FakeRenderEngine()
        val diag = FakeDiagnosticEngine(DiagnosticCategory.RUNTIME, "检查变量声明")
        val vm = createVm(pe, re, diag)
        advanceUntilIdle()
        vm.onBridgeMessage(BridgeMessage.RUNTIME_ERROR.name, "5|app.js|x is not defined")
        advanceUntilIdle()
        val log = vm.uiState.value.logs.first()
        assertEquals(DiagnosticCategory.RUNTIME, log.category)
        assertEquals("测试诊断", log.title)
        assertEquals("检查变量声明", log.suggestion)
        assertEquals(LogLevel.ERROR, log.level)
    }

    @Test
    fun `新错误触发 hasNewError 且可消费`() = runTest(testDispatcher) {
        val pe = FakeProjectEngine().apply { pushState(state) }
        val vm = createVm(pe, FakeRenderEngine())
        advanceUntilIdle()
        vm.onBridgeMessage(BridgeMessage.RUNTIME_ERROR.name, "5|app.js|boom")
        advanceUntilIdle()
        assertTrue(vm.uiState.value.hasNewError)
        vm.consumeNewError()
        assertTrue(!vm.uiState.value.hasNewError)
    }

    @Test
    fun `日志按分类过滤`() = runTest(testDispatcher) {
        val pe = FakeProjectEngine().apply { pushState(state) }
        val vm = createVm(pe, FakeRenderEngine())
        advanceUntilIdle()
        vm.onBridgeMessage(BridgeMessage.RUNTIME_ERROR.name, "5|app.js|boom") // ERROR
        vm.onBridgeMessage(BridgeMessage.CONSOLE.name, "WARN|2|careful")       // WARN
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.logs.size)
        vm.setFilter(LogFilter.ERRORS)
        assertEquals(1, vm.filteredLogs(vm.uiState.value).size)
        assertEquals(LogLevel.ERROR, vm.filteredLogs(vm.uiState.value).first().level)
        vm.setFilter(LogFilter.WARNINGS)
        assertEquals(1, vm.filteredLogs(vm.uiState.value).size)
        assertEquals(LogLevel.WARN, vm.filteredLogs(vm.uiState.value).first().level)
    }
}
