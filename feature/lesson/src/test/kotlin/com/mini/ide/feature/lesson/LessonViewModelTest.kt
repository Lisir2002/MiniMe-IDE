package com.mini.ide.feature.lesson

import com.mini.ide.core.model.FileKind
import com.mini.ide.core.model.Lesson
import com.mini.ide.core.model.LessonProgress
import com.mini.ide.core.model.LessonState
import com.mini.ide.core.model.LessonStep
import com.mini.ide.core.model.LessonTarget
import com.mini.ide.core.model.SourceFile
import com.mini.ide.core.model.StepResult
import com.mini.ide.core.model.ValidatorSpec
import com.mini.ide.testdouble.FakeFileRepository
import com.mini.ide.testdouble.FakeLessonEngine
import com.mini.ide.testdouble.FakeSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** M5 教学 ViewModel 单测：课程列表、打开、校验反馈、下一步、提示、启用。 */
@OptIn(ExperimentalCoroutinesApi::class)
class LessonViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var engine: FakeLessonEngine
    private lateinit var files: FakeFileRepository
    private lateinit var settings: FakeSettingsRepository

    private val lesson = Lesson(
        id = "l1",
        title = "测试课",
        steps = listOf(
            LessonStep("s1", "第一步", "提示1", ValidatorSpec.Contains("hello")),
            LessonStep("s2", "第二步", null, ValidatorSpec.Contains("world")),
        ),
        target = LessonTarget(description = "目标"),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        engine = FakeLessonEngine()
        files = FakeFileRepository(
            listOf(SourceFile("f1", "p1", "index.html", FileKind.HTML, "<h1>hello</h1>"))
        )
        settings = FakeSettingsRepository()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun createVm() = LessonViewModel(engine, files, settings, TestDispatchers(testDispatcher))

    @Test
    fun `loadLessons 收集课程列表`() = runTest(testDispatcher) {
        engine.pushLessons(listOf(lesson))
        val vm = createVm()
        vm.loadLessons()
        advanceUntilIdle()
        assertEquals(1, vm.uiState.first().lessons.size)
    }

    @Test
    fun `openLesson 加载课程与当前步骤`() = runTest(testDispatcher) {
        engine.pushState(LessonState(lesson, LessonProgress("l1"), lesson.steps[0]))
        val vm = createVm()
        vm.openLesson("l1")
        advanceUntilIdle()
        val state = vm.uiState.first()
        assertNotNull(state.loaded)
        assertEquals("s1", state.loaded!!.currentStep?.id)
    }

    @Test
    fun `checkStep 使用项目文件并展示校验反馈`() = runTest(testDispatcher) {
        engine.pushState(LessonState(lesson, LessonProgress("l1"), lesson.steps[0]))
        engine.stepResult = StepResult(true, "正确！")
        val vm = createVm()
        vm.openLesson("l1")
        advanceUntilIdle()
        vm.checkStep("p1")
        advanceUntilIdle()
        assertTrue(vm.uiState.first().checkResult?.passed == true)
        assertEquals("正确！", vm.uiState.first().checkResult?.feedback)
    }

    @Test
    fun `checkStep 失败时下一步不可用`() = runTest(testDispatcher) {
        engine.pushState(LessonState(lesson, LessonProgress("l1"), lesson.steps[0]))
        engine.stepResult = StepResult(false, "再试试")
        val vm = createVm()
        vm.openLesson("l1")
        advanceUntilIdle()
        vm.checkStep("p1")
        advanceUntilIdle()
        assertFalse(vm.uiState.first().checkResult?.passed == true)
        vm.nextStep()
        advanceUntilIdle()
        assertTrue(engine.advanced.isEmpty())
    }

    @Test
    fun `checkStep 通过后 nextStep 推进进度`() = runTest(testDispatcher) {
        engine.pushState(LessonState(lesson, LessonProgress("l1"), lesson.steps[0]))
        engine.stepResult = StepResult(true, "正确")
        val vm = createVm()
        vm.openLesson("l1")
        advanceUntilIdle()
        vm.checkStep("p1")
        advanceUntilIdle()
        vm.nextStep()
        advanceUntilIdle()
        assertEquals(listOf("l1"), engine.advanced)
    }

    @Test
    fun `toggleHint 切换提示可见性`() = runTest(testDispatcher) {
        val vm = createVm()
        assertFalse(vm.uiState.first().hintVisible)
        vm.toggleHint()
        assertTrue(vm.uiState.first().hintVisible)
        vm.toggleHint()
        assertFalse(vm.uiState.first().hintVisible)
    }

    @Test
    fun `enableLessons 更新设置开关`() = runTest(testDispatcher) {
        val vm = createVm()
        advanceUntilIdle()
        assertFalse(vm.uiState.first().lessonEnabled)
        vm.enableLessons()
        advanceUntilIdle()
        assertTrue(vm.uiState.first().lessonEnabled)
    }

    /** 测试用 DispatcherProvider。 */
    private class TestDispatchers(private val dispatcher: kotlinx.coroutines.CoroutineDispatcher) :
        com.mini.ide.core.common.DispatcherProvider {
        override val main = dispatcher
        override val io = dispatcher
        override val default = dispatcher
    }
}
