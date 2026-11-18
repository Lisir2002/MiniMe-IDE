package com.mini.ide.testdouble

import com.mini.ide.core.model.Project
import com.mini.ide.core.model.Settings
import com.mini.ide.core.model.SourceFile
import com.mini.ide.core.model.Template
import com.mini.ide.data.api.FileRepository
import com.mini.ide.data.api.ProjectRepository
import com.mini.ide.data.api.SettingsRepository
import com.mini.ide.data.api.TemplateRepository
import com.mini.ide.core.model.FileKind
import com.mini.ide.core.model.Lesson
import com.mini.ide.core.model.LessonProgress
import com.mini.ide.core.model.LessonState
import com.mini.ide.core.model.StepResult
import com.mini.ide.data.api.LessonRepository
import com.mini.ide.engine.api.BridgeEngine
import com.mini.ide.engine.api.BridgeHandler
import com.mini.ide.engine.api.BridgeMethod
import com.mini.ide.engine.api.Diagnostic
import com.mini.ide.engine.api.DiagnosticEngine
import com.mini.ide.engine.api.EditorEngine
import com.mini.ide.engine.api.EditorEvent
import com.mini.ide.engine.api.LessonEngine
import com.mini.ide.engine.api.ProjectEngine
import com.mini.ide.engine.api.ProjectState
import com.mini.ide.engine.api.RenderEngine
import com.mini.ide.engine.api.RenderEvent
import com.mini.ide.core.model.EditorTheme
import com.mini.ide.engine.api.PreviewDocument
import com.mini.ide.engine.api.WebViewPort
import com.mini.ide.language.api.BracketPair
import com.mini.ide.language.api.CompletionItem
import com.mini.ide.language.api.HighlightSpec
import com.mini.ide.language.api.LanguageId
import com.mini.ide.language.api.LanguageProvider
import com.mini.ide.language.api.LanguageProfile
import com.mini.ide.language.api.Snippet
import com.mini.ide.language.api.CompileResult
import com.mini.ide.language.api.CompileDiagnostic
import com.mini.ide.language.api.DslCompiler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/** 记录所有 JS 调用与加载数据的 WebView 端口，供引擎单测断言。 */
class FakeWebViewPort : WebViewPort {
    val javascripts = mutableListOf<String>()
    var loadedData: String? = null
    var loadedBaseUrl: String? = null
    var reloadCount = 0
    var jsInterface: Any? = null
    var jsInterfaceName: String? = null
    var jsCallback: ((String?) -> Unit)? = null

    override fun addJavascriptInterface(obj: Any, name: String) {
        jsInterface = obj
        jsInterfaceName = name
    }

    override fun evaluateJavascript(script: String, callback: ((String?) -> Unit)?) {
        javascripts += script
        jsCallback = callback
    }

    override fun loadDataWithBaseUrl(
        baseUrl: String?, data: String, mimeType: String, encoding: String, historyUrl: String?,
    ) {
        loadedBaseUrl = baseUrl
        loadedData = data
    }

    override fun reload() {
        reloadCount++
    }
}

/** 记录调用的桥接引擎 Fake。 */
class FakeBridgeEngine : BridgeEngine {
    val calls = mutableListOf<Pair<BridgeMethod, List<Any>>>()
    var handler: BridgeHandler? = null

    override fun install(port: WebViewPort, handler: BridgeHandler) {
        this.handler = handler
    }

    override fun call(method: BridgeMethod, vararg args: Any): String? {
        calls += method to args.toList()
        return null
    }

    override fun receive(json: String) = Unit

    override fun release() = Unit
}

/** 内存项目仓库。 */
class FakeProjectRepository(initial: List<Project> = emptyList()) : ProjectRepository {
    private val store = MutableStateFlow(initial)
    override fun observeAll(): Flow<List<Project>> = store
    override suspend fun get(id: String): Project? = store.value.firstOrNull { it.id == id }
    override suspend fun create(name: String, templateId: String?): Project {
        val p = Project(id = "p-${store.value.size + 1}", name = name, templateId = templateId)
        store.value = store.value + p
        return p
    }
    override suspend fun rename(id: String, name: String) {
        store.value = store.value.map { if (it.id == id) it.copy(name = name) else it }
    }
    override suspend fun delete(id: String) {
        store.value = store.value.filterNot { it.id == id }
    }
}

/** 内存文件仓库。 */
class FakeFileRepository(initial: List<SourceFile> = emptyList()) : FileRepository {
    private val store = MutableStateFlow(initial)
    val updated = mutableListOf<Pair<String, String>>()
    override fun observeByProject(projectId: String): Flow<List<SourceFile>> = store
    override suspend fun get(id: String): SourceFile? = store.value.firstOrNull { it.id == id }
    override suspend fun updateContent(id: String, content: String) {
        updated += id to content
        store.value = store.value.map { if (it.id == id) it.copy(content = content) else it }
    }
    override suspend fun upsertAll(files: List<SourceFile>) {
        store.value = store.value + files
    }
}

class FakeTemplateRepository : TemplateRepository {
    override suspend fun all(): List<Template> = emptyList()
    override suspend fun get(id: String): Template? = null
    override suspend fun ensureSeeded() = Unit
}

class FakeSettingsRepository : SettingsRepository {
    private val state = MutableStateFlow(Settings())
    override fun observe(): Flow<Settings> = state
    override suspend fun update(transform: (Settings) -> Settings) {
        state.value = transform(state.value)
    }
}

/** 最小语言配置，供引擎测试。 */
object FakeLanguageProfile : LanguageProfile {
    override val id = LanguageId.HTML
    override val displayName = "HTML"
    override val fileExtensions = listOf("html")
    override val codemirrorMode = "text/html"
    override val highlight = HighlightSpec(
        keywords = setOf("div", "span"),
        commentPrefix = "<!--",
        stringDelimiters = setOf('"', '\''),
    )
    override val completions = listOf(CompletionItem("div", com.mini.ide.language.api.CompletionKind.TAG, "<div></div>"))
    override val snippets = listOf(Snippet("html", "<html>$0</html>", "HTML 骨架", com.mini.ide.language.api.SnippetScope.HTML))
    override val brackets = listOf(BracketPair('(', ')'), BracketPair('{', '}'))
    override val autoCloseChars = setOf('"', '\'')
}

/** 可发出事件的编辑引擎 Fake，供 EditorViewModel 单测。 */
class FakeEditorEngine : EditorEngine {
    val loaded = mutableListOf<Triple<String, String, LanguageId>>()
    var currentText: String = ""
    private val _events = MutableSharedFlow<EditorEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<EditorEvent> = _events

    fun emit(event: EditorEvent) { _events.tryEmit(event) }

    override fun bind(port: WebViewPort, language: LanguageProfile, theme: EditorTheme) = Unit
    override fun load(fileId: String, content: String, language: LanguageProfile) {
        currentText = content
        loaded += Triple(fileId, content, language.id)
    }
    override fun currentContent(): String = currentText
    override fun insertText(text: String) { currentText += text }
    override fun undo() = Unit
    override fun redo() = Unit
    override fun setTheme(theme: EditorTheme) = Unit
    override fun setFontSize(sp: Int) = Unit
    override fun setWordWrap(enabled: Boolean) = Unit
    override fun onMessage(type: com.mini.ide.engine.api.BridgeMessage, payload: String) = Unit
    override fun release() = Unit
}

/** 记录渲染调用的渲染引擎 Fake，供 PreviewViewModel 单测。 */
class FakeRenderEngine : RenderEngine {
    val rendered = mutableListOf<PreviewDocument>()
    var reloadCount = 0
    private val _events = MutableSharedFlow<RenderEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<RenderEvent> = _events

    fun emit(event: RenderEvent) { _events.tryEmit(event) }

    override fun bind(port: WebViewPort) = Unit
    override fun render(document: PreviewDocument) { rendered += document }
    override fun reload() { reloadCount++ }
    override fun onMessage(type: com.mini.ide.engine.api.BridgeMessage, payload: String) {
        // 与 RenderEngineImpl 行为对齐：CONSOLE payload = LEVEL|line|message
        if (type == com.mini.ide.engine.api.BridgeMessage.CONSOLE) {
            val parts = payload.split("|", limit = 3)
            if (parts.size == 3) {
                val level = runCatching { com.mini.ide.engine.api.LogLevel.valueOf(parts[0]) }
                    .getOrDefault(com.mini.ide.engine.api.LogLevel.LOG)
                _events.tryEmit(RenderEvent.Console(level, parts[2], parts[1].toIntOrNull() ?: 0))
            }
        } else if (type == com.mini.ide.engine.api.BridgeMessage.RUNTIME_ERROR) {
            val parts = payload.split("|", limit = 3)
            if (parts.size == 3) {
                _events.tryEmit(RenderEvent.RuntimeError(parts[2], parts[1], parts[0].toIntOrNull() ?: 0))
            }
        }
    }
    override fun release() = Unit
}

/** 可编程的项目引擎 Fake，供 Editor/Preview ViewModel 单测。 */
class FakeProjectEngine : ProjectEngine {
    var state: ProjectState? = null
    val dirtyMarks = mutableListOf<Pair<String, String>>()
    var saveCount = 0
    var closed = false
    var previewDocument: PreviewDocument? = null
    private val _state = MutableStateFlow<ProjectState?>(null)

    fun pushState(value: ProjectState?) { state = value; _state.value = value }

    override fun open(projectId: String): Flow<ProjectState> =
        _state.filterNotNull()
    override fun observeActive(): Flow<ProjectState?> = _state
    override suspend fun saveActive() { saveCount++ }
    override fun switchFile(fileId: String) { state = state?.copy(activeFileId = fileId); _state.value = state }
    override fun markContentDirty(fileId: String, content: String) {
        dirtyMarks += fileId to content
        state = state?.copy(isDirty = true); _state.value = state
    }
    override fun currentPreviewDocument(baseUrl: String): PreviewDocument? = previewDocument
    override fun close() { closed = true }
}

/** 三语言 Provider Fake。 */
class FakeLanguageProvider : LanguageProvider {
    private val profiles = mapOf(
        FileKind.HTML to FakeLanguageProfile,
        FileKind.CSS to FakeLanguageProfile,
        FileKind.JS to FakeLanguageProfile,
    )
    override fun forKind(kind: FileKind): LanguageProfile =
        profiles[kind] ?: FakeLanguageProfile
    override fun all(): List<LanguageProfile> = profiles.values.distinct()
}

/** 内存教学仓库 Fake（M5），供 LessonEngine / LessonViewModel 单测。 */
class FakeLessonRepository(initial: List<Lesson> = emptyList()) : LessonRepository {
    private val lessons = MutableStateFlow(initial)
    private val progressMap = MutableStateFlow<Map<String, LessonProgress>>(emptyMap())
    override fun observeAll(): Flow<List<Lesson>> = lessons
    override suspend fun all(): List<Lesson> = lessons.value
    override suspend fun get(id: String): Lesson? = lessons.value.firstOrNull { it.id == id }
    override suspend fun ensureSeeded() = Unit
    fun setLessons(value: List<Lesson>) { lessons.value = value }
    override fun observeProgress(lessonId: String): Flow<LessonProgress?> =
        progressMap.map { it[lessonId] }
    override suspend fun getProgress(lessonId: String): LessonProgress? = progressMap.value[lessonId]
    override suspend fun saveProgress(progress: LessonProgress) {
        progressMap.value = progressMap.value + (progress.lessonId to progress)
    }
}

/** 可编程教学引擎 Fake（M5），供 LessonViewModel 单测。 */
class FakeLessonEngine : LessonEngine {
    var lessons: List<Lesson> = emptyList()
    var state: LessonState? = null
    var stepResult: StepResult = StepResult(true, "正确！")
    val advanced = mutableListOf<String>()
    var resetCount = 0
    private val _lessonsFlow = MutableStateFlow<List<Lesson>>(emptyList())
    private val _stateFlow = MutableStateFlow<LessonState?>(null)

    fun pushLessons(value: List<Lesson>) { lessons = value; _lessonsFlow.value = value }
    fun pushState(value: LessonState?) { state = value; _stateFlow.value = value }

    override fun observeLessons(): Flow<List<Lesson>> = _lessonsFlow
    override fun load(lessonId: String): Flow<LessonState> = _stateFlow.filterNotNull()
    override suspend fun checkStep(lessonId: String, stepId: String, files: List<SourceFile>) = stepResult
    override suspend fun advance(lessonId: String) { advanced += lessonId }
    override suspend fun reset(lessonId: String) { resetCount++ }
    override suspend fun ensureSeeded() = Unit
}

/** 固定分类的诊断引擎 Fake，供 PreviewViewModel 单测。 */
class FakeDiagnosticEngine(
    private val category: com.mini.ide.engine.api.DiagnosticCategory = com.mini.ide.engine.api.DiagnosticCategory.RUNTIME,
    private val suggestion: String? = "测试建议",
) : DiagnosticEngine {
    override fun diagnose(
        event: RenderEvent,
        currentDoc: PreviewDocument?,
    ): Diagnostic = Diagnostic(
        category = category,
        severity = when (event) {
            is RenderEvent.RuntimeError -> com.mini.ide.engine.api.LogLevel.ERROR
            is RenderEvent.Console -> event.level
            is RenderEvent.Completed -> com.mini.ide.engine.api.LogLevel.LOG
        },
        title = "测试诊断",
        detail = when (event) {
            is RenderEvent.RuntimeError -> event.message
            is RenderEvent.Console -> event.message
            is RenderEvent.Completed -> "完成"
        },
        suggestion = suggestion,
        source = when (event) {
            is RenderEvent.RuntimeError -> event.source
            else -> ""
        },
        line = when (event) {
            is RenderEvent.RuntimeError -> event.line
            is RenderEvent.Console -> event.line
            is RenderEvent.Completed -> 0
        },
    )
}

/** 可编程自研语言编译器 Fake（M6），供 ProjectEngineImpl DSL 分支单测。 */
class FakeDslCompiler(
    var result: CompileResult = CompileResult("<html></html>", "", "", emptyList()),
    var profileToReturn: LanguageProfile = FakeLanguageProfile,
) : DslCompiler {
    val compiled = mutableListOf<String>()
    override fun compile(source: String): CompileResult {
        compiled += source
        return result
    }
    override fun profile(): LanguageProfile = profileToReturn
}
