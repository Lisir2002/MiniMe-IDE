package com.mini.ide.data.api

import com.mini.ide.core.model.Lesson
import com.mini.ide.core.model.LessonProgress
import kotlinx.coroutines.flow.Flow

/**
 * S6 教学仓库（M5 激活）：课程定义 + 进度，均本地持久化。
 * 返回领域模型，不暴露 Room Entity。
 */
interface LessonRepository {
    /** 观察全部内置课程（含进度顺序），随 App 打包、幂等 seed。 */
    fun observeAll(): Flow<List<Lesson>>

    suspend fun all(): List<Lesson>
    suspend fun get(id: String): Lesson?
    suspend fun ensureSeeded()

    /** 观察某课程进度；无记录时返回 null（引擎据此创建初始进度）。 */
    fun observeProgress(lessonId: String): Flow<LessonProgress?>
    suspend fun getProgress(lessonId: String): LessonProgress?
    /** 幂等 upsert。 */
    suspend fun saveProgress(progress: LessonProgress)
}
