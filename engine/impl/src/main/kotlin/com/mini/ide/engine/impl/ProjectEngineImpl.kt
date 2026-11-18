package com.mini.ide.engine.impl

import com.mini.ide.core.model.FileKind
import com.mini.ide.core.model.Project
import com.mini.ide.core.model.SourceFile
import com.mini.ide.data.api.FileRepository
import com.mini.ide.data.api.ProjectRepository
import com.mini.ide.engine.api.PreviewDocument
import com.mini.ide.engine.api.ProjectEngine
import com.mini.ide.engine.api.ProjectState
import com.mini.ide.language.api.CompileSeverity
import com.mini.ide.language.api.DslCompiler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

/**
 * 项目引擎实现：数据层与编辑/渲染层的编排者，持有活跃项目单一真相。
 *
 * 状态管理：
 * - 数据库 Flow（files） + 内存脏镜像（_dirtyContent）合并，未保存编辑不丢失；
 * - saveActive 把脏内容写回 Repository 后清除镜像；
 * - currentPreviewDocument 从内存镜像拼三文件预览文档。
 *
 * 依赖纯接口 + 可注入 dispatcher，可用 Fake Repository + StandardTestDispatcher JVM 单测。
 */
class ProjectEngineImpl(
    private val projectRepo: ProjectRepository,
    private val fileRepo: FileRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    /** M6：自研语言编译器。默认 null 用于兼容 M2 既有构造（无 DSL 项目走原三文件逻辑）。 */
    private val dslCompiler: DslCompiler? = null,
) : ProjectEngine {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _project = MutableStateFlow<Project?>(null)
    private val _files = MutableStateFlow<List<SourceFile>>(emptyList())
    private val _activeFileId = MutableStateFlow("")
    private val _dirtyContent = MutableStateFlow<Map<String, String>>(emptyMap())

    override fun open(projectId: String): Flow<ProjectState> {
        scope.launch {
            _project.value = projectRepo.get(projectId)
            fileRepo.observeByProject(projectId).collect { dbFiles ->
                // 合并：数据库列表为骨架，内存脏内容覆盖 content
                val merged = dbFiles.map { file ->
                    _dirtyContent.value[file.id]?.let { file.copy(content = it) } ?: file
                }
                _files.value = merged
                if (_activeFileId.value.isEmpty() || merged.none { it.id == _activeFileId.value }) {
                    _activeFileId.value = merged.firstOrNull()?.id.orEmpty()
                }
            }
        }
        // combine 在 _project 异步赋值前会先发射一次 project=null 的中间态；
        // 用 mapNotNull 过滤，避免 it!! 抛 NPE 导致新建项目后进入工作区闪退。
        return observeActive().mapNotNull { it }
    }

    override fun observeActive(): Flow<ProjectState?> = combine(
        _project, _files, _activeFileId, _dirtyContent,
    ) { project, files, activeId, dirty ->
        if (project == null) null else ProjectState(
            project = project,
            files = files,
            activeFileId = activeId,
            isDirty = dirty.isNotEmpty(),
        )
    }

    override suspend fun saveActive() {
        val dirty = _dirtyContent.value
        dirty.forEach { (fileId, content) -> fileRepo.updateContent(fileId, content) }
        _dirtyContent.value = emptyMap()
    }

    override fun switchFile(fileId: String) {
        if (_files.value.any { it.id == fileId }) _activeFileId.value = fileId
    }

    override fun markContentDirty(fileId: String, content: String) {
        _dirtyContent.value = _dirtyContent.value + (fileId to content)
        // 同步内存镜像，使 observeActive 立即反映最新内容
        _files.value = _files.value.map { if (it.id == fileId) it.copy(content = content) else it }
    }

    override fun currentPreviewDocument(baseUrl: String): PreviewDocument? {
        val files = _files.value
        if (files.isEmpty()) return null
        fun contentOf(kind: FileKind) = files.firstOrNull { it.kind == kind }?.content.orEmpty()

        // M6：项目含 DSL 文件时，以编译器产物为预览源；否则维持原三文件逻辑。
        val dslFile = files.firstOrNull { it.kind == FileKind.DSL }
        if (dslFile != null && dslCompiler != null) {
            val compiled = dslCompiler.compile(dslFile.content)
            // 编译诊断经 console 通道透传，复用 M5 诊断卡片（错误/警告级才输出）
            val diagnosticJs = compiled.diagnostics
                .filter { it.severity != CompileSeverity.WARNING || it.message.contains("不存在") }
                .joinToString("\n") { d ->
                    val level = if (d.severity == CompileSeverity.ERROR) "error" else "warn"
                    val msg = "第${d.line}行 · ${d.message}".replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
                    "console.$level(\"$msg\");"
                }
            val jsWithDiagnostics = if (diagnosticJs.isBlank()) compiled.js else compiled.js + "\n" + diagnosticJs
            return PreviewDocumentBuilder.build(
                html = compiled.html,
                css = compiled.css,
                js = jsWithDiagnostics,
                baseUrl = baseUrl,
            )
        }
        return PreviewDocumentBuilder.build(
            html = contentOf(FileKind.HTML),
            css = contentOf(FileKind.CSS),
            js = contentOf(FileKind.JS),
            baseUrl = baseUrl,
        )
    }

    override fun close() {
        scope.cancel()
        _project.value = null
        _files.value = emptyList()
        _dirtyContent.value = emptyMap()
        _activeFileId.value = ""
    }
}
