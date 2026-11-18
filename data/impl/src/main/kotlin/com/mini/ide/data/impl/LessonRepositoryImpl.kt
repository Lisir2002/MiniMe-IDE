package com.mini.ide.data.impl

import com.mini.ide.core.model.Lesson
import com.mini.ide.core.model.LessonProgress
import com.mini.ide.data.api.LessonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 教学仓库实现（M5）：课程定义与进度均存 Room。
 * ensureSeeded 用 Mutex + DAO count 保证幂等，可被多处并发调用。
 */
class LessonRepositoryImpl(private val dao: LessonDao) : LessonRepository {

    private val seedMutex = Mutex()

    override fun observeAll(): Flow<List<Lesson>> =
        dao.observeAll().map { rows -> rows.map(Mappers::lesson) }

    override suspend fun all(): List<Lesson> = dao.all().map(Mappers::lesson)

    override suspend fun get(id: String): Lesson? = dao.get(id)?.let(Mappers::lesson)

    override suspend fun ensureSeeded() = seedMutex.withLock {
        if (dao.count() == 0) {
            dao.insertAll(BuiltinLessons.all().mapIndexed { i, l -> Mappers.lessonEntity(l, i) })
        }
    }

    override fun observeProgress(lessonId: String): Flow<LessonProgress?> =
        dao.observeProgress(lessonId).map { it?.let(Mappers::progress) }

    override suspend fun getProgress(lessonId: String): LessonProgress? =
        dao.getProgress(lessonId)?.let(Mappers::progress)

    override suspend fun saveProgress(progress: LessonProgress) =
        dao.saveProgress(Mappers.progressEntity(progress))
}
