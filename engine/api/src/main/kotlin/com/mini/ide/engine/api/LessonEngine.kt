package com.mini.ide.engine.api

import com.mini.ide.core.model.Lesson
import com.mini.ide.core.model.LessonState
import com.mini.ide.core.model.SourceFile
import com.mini.ide.core.model.StepResult
import kotlinx.coroutines.flow.Flow

/**
 * S6 教学引擎（M5 激活）：课程加载、分步校验、进度推进。
 * 纯编排 + 数据驱动校验，不碰 UI / WebView，可纯 JVM 单测。
 */
interface LessonEngine {
    fun observeLessons(): Flow<List<Lesson>>
    fun load(lessonId: String): Flow<LessonState>

    /** 校验某步骤：files 为当前项目三文件；规则来自课程数据定义。 */
    suspend fun checkStep(lessonId: String, stepId: String, files: List<SourceFile>): StepResult

    /** 推进到下一步（标记当前步完成 + currentStepIndex+1）。 */
    suspend fun advance(lessonId: String)

    /** 重置课程进度。 */
    suspend fun reset(lessonId: String)

    suspend fun ensureSeeded()
}
