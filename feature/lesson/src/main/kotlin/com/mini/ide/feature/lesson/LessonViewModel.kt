package com.mini.ide.feature.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mini.ide.core.common.DispatcherProvider
import com.mini.ide.core.model.Lesson
import com.mini.ide.core.model.LessonState
import com.mini.ide.core.model.SourceFile
import com.mini.ide.core.model.StepResult
import com.mini.ide.data.api.FileRepository
import com.mini.ide.data.api.SettingsRepository
import com.mini.ide.engine.api.LessonEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** 教学区 UI 状态。 */
data class LessonUiState(
    val lessons: List<Lesson> = emptyList(),
    val loaded: LessonState? = null,
    val checkResult: StepResult? = null,
    val hintVisible: Boolean = false,
    val lessonEnabled: Boolean = false,
)

/**
 * 教学 ViewModel（M5 激活）—— 设计说明 §3.1。
 * 课程列表 / 打开课程 / 分步校验 / 提示 / 下一步 / 重置 / 启用引导。
 */
@HiltViewModel
class LessonViewModel @Inject constructor(
    private val lessonEngine: LessonEngine,
    private val fileRepo: FileRepository,
    private val settingsRepo: SettingsRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonUiState())
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    init {
        // 观察设置开关（用于入口启用引导状态）
        viewModelScope.launch(dispatchers.io) {
            settingsRepo.observe().collect { settings ->
                _uiState.update { it.copy(lessonEnabled = settings.lessonEnabled) }
            }
        }
    }

    /** 加载课程列表。 */
    fun loadLessons() = viewModelScope.launch(dispatchers.io) {
        lessonEngine.ensureSeeded()
        lessonEngine.observeLessons().collect { list ->
            _uiState.update { it.copy(lessons = list) }
        }
    }

    /** 打开课程并订阅进度状态。 */
    fun openLesson(lessonId: String) = viewModelScope.launch(dispatchers.io) {
        lessonEngine.load(lessonId).collect { state ->
            _uiState.update { it.copy(loaded = state, checkResult = null, hintVisible = false) }
        }
    }

    /** 校验当前步骤：读取当前项目三文件作为上下文。 */
    fun checkStep(projectId: String) = viewModelScope.launch(dispatchers.io) {
        val state = _uiState.value.loaded ?: return@launch
        val step = state.currentStep ?: return@launch
        val files = fileRepo.observeByProject(projectId).first()
        val result = lessonEngine.checkStep(state.lesson.id, step.id, files)
        _uiState.update { it.copy(checkResult = result) }
    }

    /** 校验通过后推进下一步。 */
    fun nextStep() = viewModelScope.launch(dispatchers.io) {
        val state = _uiState.value.loaded ?: return@launch
        if (_uiState.value.checkResult?.passed != true) return@launch
        lessonEngine.advance(state.lesson.id)
    }

    fun toggleHint() = _uiState.update { it.copy(hintVisible = !it.hintVisible) }

    fun reset(lessonId: String) = viewModelScope.launch(dispatchers.io) {
        lessonEngine.reset(lessonId)
        _uiState.update { it.copy(checkResult = null, hintVisible = false) }
    }

    /** 用户确认启用教学功能。 */
    fun enableLessons() = viewModelScope.launch(dispatchers.io) {
        settingsRepo.update { it.copy(lessonEnabled = true) }
    }

    /** 供 UI 判断某步骤是否已完成。 */
    suspend fun isStepCompleted(lessonId: String, stepId: String): Boolean =
        withContext(dispatchers.io) {
            // 进度已包含在 loaded 状态中，避免额外 IO
            _uiState.value.loaded?.progress?.completedStepIds?.contains(stepId) ?: false
        }
}
