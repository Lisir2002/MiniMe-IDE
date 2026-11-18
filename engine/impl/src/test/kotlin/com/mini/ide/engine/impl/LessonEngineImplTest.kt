package com.mini.ide.engine.impl

import com.mini.ide.core.model.Lesson
import com.mini.ide.core.model.LessonProgress
import com.mini.ide.core.model.LessonStep
import com.mini.ide.core.model.LessonTarget
import com.mini.ide.core.model.SourceFile
import com.mini.ide.core.model.ValidatorSpec
import com.mini.ide.testdouble.FakeLessonRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** M5 教学引擎单测：load 状态、checkStep 校验、advance 推进、reset 重置。 */
@OptIn(ExperimentalCoroutinesApi::class)
class LessonEngineImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val lesson = Lesson(
        id = "l1",
        title = "测试课",
        steps = listOf(
            LessonStep("s1", "第一步", "提示1", ValidatorSpec.Contains("<h1")),
            LessonStep("s2", "第二步", null, ValidatorSpec.Contains("color")),
        ),
        target = LessonTarget(description = "目标"),
    )

    private fun engine(repo: FakeLessonRepository) = LessonEngineImpl(repo, dispatcher)

    @Test
    fun `load 无进度时给出初始状态与第一步`() = runTest(dispatcher) {
        val repo = FakeLessonRepository(listOf(lesson))
        val eng = engine(repo)
        val state = eng.load("l1").first()
        assertEquals("l1", state.lesson.id)
        assertEquals(0, state.progress.currentStepIndex)
        assertEquals("s1", state.currentStep?.id)
    }

    @Test
    fun `load 合并已有进度并定位当前步`() = runTest(dispatcher) {
        val repo = FakeLessonRepository(listOf(lesson))
        repo.saveProgress(LessonProgress("l1", 1, setOf("s1"), 10L, 20L))
        val state = engine(repo).load("l1").first()
        assertEquals(1, state.progress.currentStepIndex)
        assertEquals("s2", state.currentStep?.id)
    }

    @Test
    fun `checkStep 依据规则返回通过或失败`() = runTest(dispatcher) {
        val repo = FakeLessonRepository(listOf(lesson))
        val eng = engine(repo)
        val files = listOf(SourceFile("f", "p", "index.html", com.mini.ide.core.model.FileKind.HTML, "<h1>hi</h1>"))
        assertTrue(eng.checkStep("l1", "s1", files).passed)
        assertTrue(!eng.checkStep("l1", "s2", files).passed)
    }

    @Test
    fun `checkStep 课程或步骤不存在返回失败`() = runTest(dispatcher) {
        val eng = engine(FakeLessonRepository(listOf(lesson)))
        assertTrue(!eng.checkStep("missing", "s1", emptyList()).passed)
        assertTrue(!eng.checkStep("l1", "missing", emptyList()).passed)
    }

    @Test
    fun `advance 推进到下一步并记录完成步骤`() = runTest(dispatcher) {
        val repo = FakeLessonRepository(listOf(lesson))
        val eng = engine(repo)
        eng.advance("l1")
        advanceUntilIdle()
        val p = repo.getProgress("l1")
        assertNotNull(p)
        assertEquals(1, p!!.currentStepIndex)
        assertEquals(setOf("s1"), p.completedStepIds)
    }

    @Test
    fun `advance 到达末尾不越界`() = runTest(dispatcher) {
        val repo = FakeLessonRepository(listOf(lesson))
        repo.saveProgress(LessonProgress("l1", 1, setOf("s1")))
        val eng = engine(repo)
        eng.advance("l1")
        advanceUntilIdle()
        assertEquals(2, repo.getProgress("l1")!!.currentStepIndex)
        assertEquals(setOf("s1", "s2"), repo.getProgress("l1")!!.completedStepIds)
    }

    @Test
    fun `reset 清空进度`() = runTest(dispatcher) {
        val repo = FakeLessonRepository(listOf(lesson))
        repo.saveProgress(LessonProgress("l1", 1, setOf("s1"), 10L, 20L))
        val eng = engine(repo)
        eng.reset("l1")
        advanceUntilIdle()
        val p = repo.getProgress("l1")!!
        assertEquals(0, p.currentStepIndex)
        assertTrue(p.completedStepIds.isEmpty())
    }
}
