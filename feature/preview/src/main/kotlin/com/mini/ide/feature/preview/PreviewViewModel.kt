package com.mini.ide.feature.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mini.ide.core.common.DispatcherProvider
import com.mini.ide.data.api.SettingsRepository
import com.mini.ide.engine.api.BridgeMessage
import com.mini.ide.engine.api.DiagnosticCategory
import com.mini.ide.engine.api.DiagnosticEngine
import com.mini.ide.engine.api.LogLevel
import com.mini.ide.engine.api.PreviewDocument
import com.mini.ide.engine.api.ProjectEngine
import com.mini.ide.engine.api.RenderEngine
import com.mini.ide.engine.api.RenderEvent
import com.mini.ide.engine.api.WebViewPort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** 一条预览日志（M5 升级为结构化诊断，字段带默认值向后兼容）。 */
data class PreviewLog(
    val level: LogLevel,
    val message: String,
    val source: String = "",
    val line: Int = 0,
    val category: DiagnosticCategory = DiagnosticCategory.UNKNOWN,
    val title: String = "",
    val suggestion: String? = null,
)

/** 日志分类过滤。 */
enum class LogFilter { ALL, ERRORS, WARNINGS }

/** 预览区 UI 状态。 */
data class PreviewUiState(
    val logs: List<PreviewLog> = emptyList(),
    val lastRenderMs: Long = 0,
    val filter: LogFilter = LogFilter.ALL,
    val hasNewError: Boolean = false,
)

/**
 * 预览区 ViewModel —— 设计说明 §3.3。
 * 唯一渲染触发方：观察项目脏状态 → 防抖 → 拼装预览文档 → 渲染。
 * 日志来自 RenderEngine.events 与 PreviewJsBridge 两路，统一聚合。
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val projectEngine: ProjectEngine,
    private val renderEngine: RenderEngine,
    private val settingsRepo: SettingsRepository,
    private val diagnosticEngine: DiagnosticEngine,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreviewUiState())
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()

    private val baseUrl = "file:///android_asset/preview/"
    private var currentDoc: PreviewDocument? = null

    init {
        // 聚合 RenderEngine 事件（含 Completed 计时），经诊断引擎升级为结构化信息
        viewModelScope.launch(dispatchers.main) {
            renderEngine.events.collect { event ->
                when (event) {
                    is RenderEvent.Console, is RenderEvent.RuntimeError -> {
                        val diagnostic = diagnosticEngine.diagnose(event, currentDoc)
                        appendLog(
                            PreviewLog(
                                level = diagnostic.severity,
                                message = diagnostic.detail,
                                source = diagnostic.source,
                                line = diagnostic.line,
                                category = diagnostic.category,
                                title = diagnostic.title,
                                suggestion = diagnostic.suggestion,
                            )
                        )
                    }
                    is RenderEvent.Completed -> _uiState.update { it.copy(lastRenderMs = event.durationMs) }
                }
            }
        }
    }

    /** 预览 WebView 就绪后绑定渲染引擎。 */
    fun attach(port: WebViewPort) = renderEngine.bind(port)

    /** 打开项目：观察脏状态，防抖后渲染（防抖时长取打开时的设置快照）。 */
    fun open(projectId: String) = viewModelScope.launch(dispatchers.io) {
        val debounceMs = settingsRepo.observe().map { it.previewDebounceMs }.first()
        projectEngine.open(projectId).debounce(debounceMs.coerceAtLeast(0L)).collect { renderOnce() }
    }

    /** 立即渲染一次（手动运行）。 */
    fun run() = viewModelScope.launch(dispatchers.io) { renderOnce() }

    /** 重新加载：WebView 方法必须在主线程。 */
    fun reload() = viewModelScope.launch(dispatchers.main) { renderEngine.reload() }

    /** 预览桥接消息：转发给 RenderEngine 处理并聚合日志。 */
    fun onBridgeMessage(type: String, payload: String) {
        val msg = runCatching { BridgeMessage.valueOf(type) }.getOrNull() ?: return
        renderEngine.onMessage(msg, payload)
    }

    /**
     * 渲染一次。文档拼装（纯内存）可在调用方线程完成，
     * 但 render 内部会调用 WebViewPort.loadDataWithBaseUrl——WebView 方法必须在主线程，
     * 否则触发 `A WebView method was called on thread ...` 崩溃（rc1/crash 复盘）。
     */
    private suspend fun renderOnce() {
        val document = projectEngine.currentPreviewDocument(baseUrl) ?: return
        currentDoc = document
        withContext(dispatchers.main) {
            renderEngine.render(PreviewBridgeScript.wrap(document))
        }
    }

    private fun appendLog(log: PreviewLog) {
        _uiState.update {
            it.copy(
                logs = (it.logs + log).takeLast(50),
                hasNewError = it.hasNewError || log.level == LogLevel.ERROR,
            )
        }
    }

    fun clearLogs() = _uiState.update { it.copy(logs = emptyList(), hasNewError = false) }

    fun setFilter(filter: LogFilter) = _uiState.update { it.copy(filter = filter) }

    /** UI 展开日志面板后消费"新错误"提示。 */
    fun consumeNewError() = _uiState.update { it.copy(hasNewError = false) }

    /** 按当前过滤条件返回日志。 */
    fun filteredLogs(state: PreviewUiState): List<PreviewLog> = when (state.filter) {
        LogFilter.ALL -> state.logs
        LogFilter.ERRORS -> state.logs.filter { it.level == LogLevel.ERROR }
        LogFilter.WARNINGS -> state.logs.filter { it.level == LogLevel.WARN }
    }
}
