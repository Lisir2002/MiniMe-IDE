package com.mini.ide.data.impl

import com.mini.ide.core.model.Project
import com.mini.ide.core.model.SourceFile
import com.mini.ide.core.model.SourceFileDraft
import com.mini.ide.core.model.Template
import com.mini.ide.data.api.FileRepository
import com.mini.ide.data.api.ProjectRepository
import com.mini.ide.data.api.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/** 项目仓库实现。create 时若指定模板，会在事务内一并实例化模板文件。 */
class ProjectRepositoryImpl(
    private val db: MiniMeDatabase,
    private val templates: TemplateRepository,
) : ProjectRepository {

    private val projectDao get() = db.projectDao()

    override fun observeAll(): Flow<List<Project>> =
        projectDao.observeAll().map { list -> list.map(Mappers::project) }

    override suspend fun get(id: String): Project? = projectDao.get(id)?.let(Mappers::project)

    override suspend fun create(name: String, templateId: String?): Project {
        val now = System.currentTimeMillis()
        val project = Project(
            id = UUID.randomUUID().toString(),
            name = name,
            templateId = templateId,
            createdAt = now,
            updatedAt = now,
        )
        val drafts: List<SourceFileDraft> = templateId?.let { templates.get(it)?.files }.orEmpty()
        val files = drafts.map { draft ->
            Mappers.fileEntity(
                SourceFile(
                    id = UUID.randomUUID().toString(),
                    projectId = project.id,
                    name = draft.name,
                    kind = draft.kind,
                    content = draft.content,
                    sortOrder = draft.sortOrder,
                )
            )
        }
        projectDao.insertWithFiles(Mappers.projectEntity(project), files)
        return project
    }

    override suspend fun rename(id: String, name: String) =
        projectDao.rename(id, name, System.currentTimeMillis())

    override suspend fun delete(id: String) = projectDao.delete(id)
}

/** 文件仓库实现。内容读写经 ContentStore 抽象，保留可替换性。 */
class FileRepositoryImpl(
    private val db: MiniMeDatabase,
    private val contentStore: ContentStore,
) : FileRepository {

    private val fileDao get() = db.fileDao()

    override fun observeByProject(projectId: String): Flow<List<SourceFile>> =
        fileDao.observeByProject(projectId).map { list -> list.map(Mappers::file) }

    override suspend fun get(id: String): SourceFile? = fileDao.get(id)?.let(Mappers::file)

    override suspend fun updateContent(id: String, content: String) = contentStore.write(id, content)

    override suspend fun upsertAll(files: List<SourceFile>) =
        fileDao.upsertAll(files.map(Mappers::fileEntity))
}

/** 模板仓库实现。ensureSeeded 幂等：仅当表为空时注入内置模板。 */
class TemplateRepositoryImpl(private val db: MiniMeDatabase) : TemplateRepository {
    private val dao get() = db.templateDao()

    override suspend fun all(): List<Template> = dao.all().map(Mappers::template)

    override suspend fun get(id: String): Template? = dao.get(id)?.let(Mappers::template)

    override suspend fun ensureSeeded() {
        if (dao.count() > 0) return
        dao.insertAll(BuiltinTemplates.all().map(Mappers::templateEntity))
    }
}
