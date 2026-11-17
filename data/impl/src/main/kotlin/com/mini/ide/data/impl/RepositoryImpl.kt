package com.mini.ide.data.impl

import com.mini.ide.data.api.ProjectRepository
import com.mini.ide.core.model.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class ProjectRepositoryImpl : ProjectRepository {
    override fun observeAll(): Flow<List<Project>> = emptyFlow()
}
