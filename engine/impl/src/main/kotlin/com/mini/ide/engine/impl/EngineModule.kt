package com.mini.ide.engine.impl

import com.mini.ide.core.common.DispatcherProvider
import com.mini.ide.data.api.FileRepository
import com.mini.ide.data.api.LessonRepository
import com.mini.ide.data.api.ProjectRepository
import com.mini.ide.engine.api.BridgeEngine
import com.mini.ide.engine.api.BridgeHandler
import com.mini.ide.engine.api.DiagnosticEngine
import com.mini.ide.engine.api.EditorEngine
import com.mini.ide.engine.api.LessonEngine
import com.mini.ide.engine.api.ProjectEngine
import com.mini.ide.engine.api.RenderEngine
import com.mini.ide.language.api.DslCompiler
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

    /** 桥接消息扇出：编辑引擎 + 渲染引擎都作为 BridgeHandler。 */
    @Provides
    @Singleton
    fun provideBridgeHandler(editor: EditorEngine, render: RenderEngine): BridgeHandler =
        FanoutBridgeHandler(listOf(editor, render))

    @Provides
    @Singleton
    fun provideProjectEngine(
        projectRepo: ProjectRepository,
        fileRepo: FileRepository,
        dslCompiler: DslCompiler,
    ): ProjectEngine = ProjectEngineImpl(projectRepo, fileRepo, dslCompiler = dslCompiler)

    /** M5 教学引擎。 */
    @Provides
    @Singleton
    fun provideLessonEngine(
        lessons: LessonRepository,
        dispatchers: DispatcherProvider,
    ): LessonEngine = LessonEngineImpl(lessons, dispatchers.io)

    /** M5 错误诊断引擎。 */
    @Provides
    @Singleton
    fun provideDiagnosticEngine(): DiagnosticEngine = DiagnosticEngineImpl()
}
