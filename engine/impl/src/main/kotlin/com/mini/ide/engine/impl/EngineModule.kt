package com.mini.ide.engine.impl

import com.mini.ide.data.api.FileRepository
import com.mini.ide.data.api.ProjectRepository
import com.mini.ide.engine.api.BridgeEngine
import com.mini.ide.engine.api.EditorEngine
import com.mini.ide.engine.api.ProjectEngine
import com.mini.ide.engine.api.RenderEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @Singleton
    fun provideBridgeEngine(): BridgeEngine = BridgeEngineImpl()

    @Provides
    @Singleton
    fun provideEditorEngine(bridge: BridgeEngine): EditorEngine =
        EditorEngineImpl(bridge)

    @Provides
    @Singleton
    fun provideRenderEngine(): RenderEngine = RenderEngineImpl()

    @Provides
    @Singleton
    fun provideProjectEngine(
        projectRepo: ProjectRepository,
        fileRepo: FileRepository,
    ): ProjectEngine = ProjectEngineImpl(projectRepo, fileRepo)
}
