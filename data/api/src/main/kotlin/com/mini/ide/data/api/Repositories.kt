package com.mini.ide.data.api

import com.mini.ide.core.model.Project
import com.mini.ide.core.model.Settings
import com.mini.ide.core.model.SourceFile
import com.mini.ide.core.model.Template
import kotlinx.coroutines.flow.Flow

/** 项目仓库：项目的增删改查。返回领域模型，不暴露 Room Entity。 */
interface ProjectRepository {
    fun observeAll(): Flow<List<Project>>
    suspend fun get(id: String): Project?
    suspend fun create(name: String, templateId: String? = null): Project
    suspend fun rename(id: String, name: String)
    suspend fun delete(id: String)
}

/** 文件仓库：源码文件的读取与更新。 */
interface FileRepository {
    fun observeByProject(projectId: String): Flow<List<SourceFile>>
    suspend fun get(id: String): SourceFile?
    suspend fun updateContent(id: String, content: String)
    /** 模板实例化：一次性写入项目的全部初始文件（事务）。 */
    suspend fun upsertAll(files: List<SourceFile>)
}

/** 模板仓库：内置模板从 assets 注入（ensureSeeded 幂等）。 */
interface TemplateRepository {
    suspend fun all(): List<Template>
    suspend fun get(id: String): Template?
    suspend fun ensureSeeded()
}

/** 设置仓库：以 Flow 暴露，更新用 transform 保证读改写原子性。 */
interface SettingsRepository {
    fun observe(): Flow<Settings>
    suspend fun update(transform: (Settings) -> Settings)
}
