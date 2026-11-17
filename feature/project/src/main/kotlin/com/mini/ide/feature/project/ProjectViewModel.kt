package com.mini.ide.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mini.ide.core.common.DispatcherProvider
import com.mini.ide.core.model.Project
import com.mini.ide.core.model.Template
import com.mini.ide.data.api.ProjectRepository
import com.mini.ide.data.api.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 项目列表 UI 状态。 */
data class ProjectUiState(
    val projects: List<Project> = emptyList(),
    val templates: List<Template> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * 项目列表 ViewModel —— 对应设计说明 §3.1。
 * 只依赖 data:api 与 core:common，可用 Fake Repository + TestDispatcher 纯 JVM 单测。
 */
@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectRepo: ProjectRepository,
    private val templateRepo: TemplateRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

    /** 打开项目的导航信号（携带项目 id）。 */
    private val _openedProject = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val openedProject: SharedFlow<String> = _openedProject.asSharedFlow()

    /** 进入列表：幂等 seed 模板 + 观察项目 + 拉取模板。 */
    fun load() = viewModelScope.launch(dispatchers.io) {
        templateRepo.ensureSeeded()
        val templates = templateRepo.all()
        _uiState.update { it.copy(templates = templates) }
        launch(dispatchers.io) {
            projectRepo.observeAll().collect { list ->
                _uiState.update { it.copy(projects = list.sortedByDescending { p -> p.updatedAt }, isLoading = false) }
            }
        }
    }

    /**
     * 新建项目：始终绑定一个模板以保证三文件存在（默认第一个内置模板）。
     * 创建成功后发出打开信号。
     */
    fun create(name: String, templateId: String? = null) = viewModelScope.launch(dispatchers.io) {
        val finalTemplateId = templateId
            ?: _uiState.value.templates.firstOrNull()?.id
            ?: run {
                // 极端兜底：模板表为空则再次 seed 后取
                templateRepo.ensureSeeded()
                templateRepo.all().firstOrNull()?.id
            }
        val project = projectRepo.create(name, finalTemplateId)
        _openedProject.emit(project.id)
    }

    fun rename(id: String, name: String) = viewModelScope.launch(dispatchers.io) {
        projectRepo.rename(id, name)
    }

    fun delete(id: String) = viewModelScope.launch(dispatchers.io) {
        projectRepo.delete(id)
    }

    fun open(id: String) = viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
        _openedProject.emit(id)
    }
}
