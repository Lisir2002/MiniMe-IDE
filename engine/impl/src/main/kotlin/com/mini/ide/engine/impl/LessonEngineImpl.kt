package com.mini.ide.engine.impl

import com.mini.ide.core.model.CheckContext
import com.mini.ide.core.model.Lesson
import com.mini.ide.core.model.LessonProgress
import com.mini.ide.core.model.LessonState
import com.mini.ide.core.model.SourceFile
import com.mini.ide.core.model.StepResult
import com.mini.ide.data.api.LessonRepository
import com.mini.ide.engine.api.LessonEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext

/**
 * 教学引擎实现（M5）：编排课程仓库 + 进度，数据驱动校验。
 * 只依赖 data:api + core:model + 可注入 dispatcher，纯 JVM 单测。
 */
class LessonEngineImpl(
    private val lessons: LessonRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : LessonEngine {

    override fun observeLessons(): Flow<List<Lesson>> = lessons.observeAll()

    override suspend fun ensureSeeded() = lessons.ensureSeeded()

    override fun load(lessonId: String): Flow<LessonState> = combine(
        lessons.observeAll(),
        lessons.observeProgress(lessonId),
    ) { all, progress ->
        val lesson = all.firstOrNull { it.id == lessonId } ?: return@combine null
        val effective = progress ?: LessonProgress(
            lessonId = lessonId,
            startedAt = System.currentTimeMillis(),
        )
        val step = lesson.steps.getOrNull(effective.currentStepIndex)
        LessonState(lesson, effective, step)
    }.filterNotNull()

    override suspend fun checkStep(
        lessonId: String,
        stepId: String,
        files: List<SourceFile>,
    ): StepResult = withContext(dispatcher) {
        val lesson = lessons.get(lessonId)
            ?: return@withContext StepResult(false, "课程不存在，请重新选择。")
        val step = lesson.steps.firstOrNull { it.id == stepId }
            ?: return@withContext StepResult(false, "步骤不存在，请刷新课程。")
        StepValidator.validate(step.validator, CheckContext(files))
    }

    override suspend fun advance(lessonId: String) = withContext(dispatcher) {
        val lesson = lessons.get(lessonId) ?: return@withContext
        val current = lessons.getProgress(lessonId) ?: LessonProgress(
            lessonId = lessonId,
            startedAt = System.currentTimeMillis(),
        )
        val step = lesson.steps.getOrNull(current.currentStepIndex) ?: return@withContext
        val nextIndex = (current.currentStepIndex + 1).coerceAtMost(lesson.steps.size)
        lessons.saveProgress(
            current.copy(
                currentStepIndex = nextIndex,
                completedStepIds = current.completedStepIds + step.id,
                updatedAt = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun reset(lessonId: String) = withContext(dispatcher) {
        lessons.saveProgress(
            LessonProgress(
                lessonId = lessonId,
                currentStepIndex = 0,
                completedStepIds = emptySet(),
                startedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        )
    }
}
