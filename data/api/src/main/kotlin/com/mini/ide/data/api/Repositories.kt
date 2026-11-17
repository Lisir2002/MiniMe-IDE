package com.mini.ide.data.api

import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun observeAll(): Flow<List<com.mini.ide.core.model.Project>>
}

interface FileRepository {
    fun observeByProject(projectId: String): Flow<List<com.mini.ide.core.model.SourceFile>>
}
