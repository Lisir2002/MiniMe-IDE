package com.mini.ide.feature.project

import com.mini.ide.core.common.DispatcherProvider
import com.mini.ide.core.model.Project
import com.mini.ide.core.model.SourceFileDraft
import com.mini.ide.core.model.Template
import com.mini.ide.testdouble.FakeProjectRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = testDispatcher
        override val io: CoroutineDispatcher = testDispatcher
        override val default: CoroutineDispatcher = testDispatcher
    }
    private val template = Template(
        id = "tpl-blank",
        name = "空白三件套",
        description = "",
        files = listOf(
            SourceFileDraft("index.html", com.mini.ide.core.model.FileKind.HTML, "<h1>Hi</h1>"),
            SourceFileDraft("style.css", com.mini.ide.core.model.FileKind.CSS, "body{}"),
            SourceFileDraft("app.js", com.mini.ide.core.model.FileKind.JS, ""),
        ),
        isBuiltin = true,
    )
    private val templateRepo = object : com.mini.ide.data.api.TemplateRepository {
        var seeded = false
        override suspend fun all() = listOf(template)
        override suspend fun get(id: String) = if (id == template.id) template else null
        override suspend fun ensureSeeded() { seeded = true }
    }
    private val fileRepo = object : com.mini.ide.data.api.FileRepository {
        override fun observeByProject(projectId: String): Flow<List<com.mini.ide.core.model.SourceFile>> = kotlinx.coroutines.flow.emptyFlow()
        override suspend fun get(id: String) = null
        override suspend fun updateContent(id: String, content: String) = Unit
        override suspend fun upsertAll(files: List<com.mini.ide.core.model.SourceFile>) = Unit
    }

    @Test
    fun `load seeds templates and observes projects`() = runTest(testDispatcher) {
        val repo = FakeProjectRepository(initial = listOf(Project("p1", "作品一", updatedAt = 100)))
        val vm = ProjectViewModel(repo, templateRepo, dispatchers)
        vm.load()
        advanceUntilIdle()
        assertTrue(templateRepo.seeded)
        assertEquals(1, vm.uiState.value.projects.size)
        assertEquals("作品一", vm.uiState.value.projects.first().name)
        assertEquals(listOf(template), vm.uiState.value.templates)
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @Test
    fun `create binds default template and emits open signal`() = runTest(testDispatcher) {
        val repo = FakeProjectRepository()
        val vm = ProjectViewModel(repo, templateRepo, dispatchers)
        vm.load()
        advanceUntilIdle()
        // 先订阅再触发（SharedFlow 无 replay）
        val opened = mutableListOf<String>()
        val collector = launch { vm.openedProject.collect { opened += it } }
        advanceUntilIdle()
        vm.create("新作品")
        advanceUntilIdle()
        assertEquals(1, repo.observeAll().first().size)
        assertEquals("新作品", repo.observeAll().first().first().name)
        assertEquals("tpl-blank", repo.observeAll().first().first().templateId)
        assertTrue(opened.first().isNotBlank())
        collector.cancel()
    }

    @Test
    fun `delete removes project from list`() = runTest(testDispatcher) {
        val repo = FakeProjectRepository(initial = listOf(Project("p1", "待删除")))
        val vm = ProjectViewModel(repo, templateRepo, dispatchers)
        vm.load()
        advanceUntilIdle()
        vm.delete("p1")
        advanceUntilIdle()
        assertTrue(repo.observeAll().first().isEmpty())
        assertTrue(vm.uiState.value.projects.isEmpty())
    }
}
