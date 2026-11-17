package com.mini.ide.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mini.ide.core.common.DispatcherProvider
import com.mini.ide.core.model.EditorTheme
import com.mini.ide.core.model.FileKind
import com.mini.ide.core.model.SourceFile
import com.mini.ide.data.api.SettingsRepository
import com.mini.ide.engine.api.BridgeEngine
import com.mini.ide.engine.api.BridgeHandler
import com.mini.ide.engine.api.EditorEngine
import com.mini.ide.engine.api.EditorEvent
import com.mini.ide.engine.api.ProjectEngine
import com.mini.ide.engine.api.WebViewPort
import com.mini.ide.language.api.LanguageProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** 编辑区 UI 状态。 */
data class EditorUiState(
    val files: List<SourceFile> = emptyList(),
    val activeFileId: String = "",
    val projectName: String = "",
    val line: Int = 1,
    val column: Int = 1,
    val isDirty: Boolean = false,
)

/**
 * 编辑区 ViewModel —— 设计说明 §3.2。
 * 编排 ProjectEngine（状态）+ EditorEngine（编辑）+ BridgeEngine（JS 通道）。
 * 只依赖 api 接口，可用 Fake 引擎 + TestDispatcher 纯 JVM 单测。
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class EditorViewModel @Inject constructor(
    private val projectEngine: ProjectEngine,
    private val editorEngine: EditorEngine,
    private val bridgeEngine: BridgeEngine,
    private val bridgeHandler: BridgeHandler,
    private val languageProvider: LanguageProvider,
    private val settingsRepo: SettingsRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _dirtySignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private var lastLoadedFileId = ""

    init {
        // 编辑器事件：内容变化 → 脏标记 + 自动保存信号；光标 → UI
        viewModelScope.launch(dispatchers.main) {
            editorEngine.events.collect { event ->
                when (event) {
                    is EditorEvent.ContentChanged -> {
                        projectEngine.markContentDirty(event.fileId, event.content)
                        _uiState.update { it.copy(isDirty = true) }
                        _dirtySignal.tryEmit(Unit)
                    }
                    is EditorEvent.CursorMoved ->
                        _uiState.update { it.copy(line = event.line, column = event.column) }
                    is EditorEvent.Ready -> {
                        // WebView 页面脚本就绪后重发当前文件，规避异步加载导致的首次 SET_CONTENT 丢失
                        val active = _uiState.value.files.firstOrNull { it.id == _uiState.value.activeFileId }
                        if (active != null) {
                            lastLoadedFileId = active.id
                            editorEngine.load(active.id, active.content, languageProvider.forKind(active.kind))
                        }
                    }
                }
            }
        }
        // 自动保存：设置开启时，停止输入 debounce 后写回；关闭则不订阅 dirty 信号
        viewModelScope.launch(dispatchers.io) {
            settingsRepo.observe().flatMapLatest { settings ->
                if (settings.autoSave) {
                    _dirtySignal.debounce(settings.previewDebounceMs.coerceAtLeast(300L)).map { Unit }
                } else {
                    flow { }
                }
            }.collect { projectEngine.saveActive() }
        }
    }

    /** WebView 就绪后绑定桥接与引擎。 */
    fun attach(port: WebViewPort) {
        bridgeEngine.install(port, bridgeHandler)
        val profile = languageProvider.forKind(FileKind.HTML)
        editorEngine.bind(port, profile, EditorTheme.DEFAULT_DARK)
    }

    /** 打开项目并观察状态。 */
    fun open(projectId: String) = viewModelScope.launch(dispatchers.io) {
        projectEngine.open(projectId).collect { state ->
            _uiState.update {
                it.copy(
                    files = state.files,
                    activeFileId = state.activeFileId,
                    projectName = state.project.name,
                    isDirty = state.isDirty,
                )
            }
            val active = state.files.firstOrNull { f -> f.id == state.activeFileId }
            if (active != null && active.id != lastLoadedFileId) {
                lastLoadedFileId = active.id
                withContext(dispatchers.main) {
                    editorEngine.load(active.id, active.content, languageProvider.forKind(active.kind))
                }
            }
        }
    }

    /** 切换文件：立即加载目标文件内容。 */
    fun switchFile(fileId: String) {
        val file = _uiState.value.files.firstOrNull { it.id == fileId } ?: return
        lastLoadedFileId = fileId
        editorEngine.load(fileId, file.content, languageProvider.forKind(file.kind))
        projectEngine.switchFile(fileId)
        _uiState.update { it.copy(activeFileId = fileId, isDirty = false) }
    }

    fun undo() = editorEngine.undo()
    fun redo() = editorEngine.redo()
    fun insert(text: String) = editorEngine.insertText(text)
    fun save() = viewModelScope.launch(dispatchers.io) { projectEngine.saveActive() }

    fun release() {
        bridgeEngine.release()
        editorEngine.release()
        projectEngine.close()
    }
}
