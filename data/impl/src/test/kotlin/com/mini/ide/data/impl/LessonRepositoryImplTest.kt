package com.mini.ide.data.impl

import androidx.test.core.app.ApplicationProvider
import com.mini.ide.core.model.LessonProgress
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** M5 教学仓库单测：课程 seed 幂等、进度 upsert、步骤校验规则往返序列化。 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LessonRepositoryImplTest {

    private lateinit var db: MiniMeDatabase
    private lateinit var lessons: LessonRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = MiniMeDatabase.buildInMemory(context)
        lessons = LessonRepositoryImpl(db.lessonDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `课程 seed 幂等且含两节内置课`() = runBlocking {
        lessons.ensureSeeded()
        val all = lessons.all()
        assertEquals(2, all.size)
        assertTrue(all.any { it.id == "lesson-hello" })
        assertTrue(all.any { it.id == "lesson-style" })
        // 再次 seed 不重复
        lessons.ensureSeeded()
        assertEquals(2, lessons.all().size)
    }

    @Test
    fun `课程 steps 与校验规则往返序列化`() = runBlocking {
        lessons.ensureSeeded()
        val hello = lessons.get("lesson-hello")
        assertNotNull(hello)
        assertEquals(3, hello!!.steps.size)
        // 三种 ValidatorSpec 都能还原（Contains / Regex / AstRule）
        val types = hello.steps.map { it.validator::class.simpleName }.toSet()
        assertTrue(types.contains("Contains"))
        assertTrue(types.contains("Regex"))
        assertTrue(types.contains("AstRule"))
        assertTrue(hello.steps[0].hint!!.isNotBlank())
    }

    @Test
    fun `课程列表通过 Flow 观察`() = runBlocking {
        lessons.ensureSeeded()
        assertEquals(2, lessons.observeAll().first().size)
    }

    @Test
    fun `进度读写与 upsert 幂等`() = runBlocking {
        val p = LessonProgress(
            lessonId = "lesson-hello",
            currentStepIndex = 1,
            completedStepIds = setOf("s1"),
            startedAt = 100L,
            updatedAt = 200L,
        )
        lessons.saveProgress(p)
        val loaded = lessons.getProgress("lesson-hello")
        assertNotNull(loaded)
        assertEquals(1, loaded!!.currentStepIndex)
        assertEquals(setOf("s1"), loaded.completedStepIds)

        // upsert：更新同一课程进度
        lessons.saveProgress(loaded.copy(currentStepIndex = 2, completedStepIds = setOf("s1", "s2")))
        val updated = lessons.getProgress("lesson-hello")!!
        assertEquals(2, updated.currentStepIndex)
        assertEquals(setOf("s1", "s2"), updated.completedStepIds)
        // observeProgress 同步
        assertEquals(2, lessons.observeProgress("lesson-hello").first()!!.currentStepIndex)
    }

    @Test
    fun `无进度时返回 null`() = runBlocking {
        assertNull(lessons.getProgress("lesson-style"))
    }
}
