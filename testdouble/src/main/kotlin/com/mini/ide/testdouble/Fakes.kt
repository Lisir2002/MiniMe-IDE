package com.mini.ide.testdouble

import com.mini.ide.core.model.Project
import com.mini.ide.core.model.Settings
import com.mini.ide.core.model.SourceFile
import com.mini.ide.core.model.Template
import com.mini.ide.data.api.FileRepository
import com.mini.ide.data.api.ProjectRepository
import com.mini.ide.data.api.SettingsRepository
import com.mini.ide.data.api.TemplateRepository
import com.mini.ide.engine.api.BridgeEngine
import com.mini.ide.engine.api.BridgeHandler
import com.mini.ide.engine.api.BridgeMethod
import com.mini.ide.engine.api.WebViewPort
import com.mini.ide.language.api.BracketPair
import com.mini.ide.language.api.CompletionItem
import com.mini.ide.language.api.HighlightSpec
import com.mini.ide.language.api.LanguageId
import com.mini.ide.language.api.LanguageProfile
import com.mini.ide.language.api.Snippet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

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
