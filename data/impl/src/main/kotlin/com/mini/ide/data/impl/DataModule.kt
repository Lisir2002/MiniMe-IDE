package com.mini.ide.data.impl

import android.content.Context
import com.mini.ide.data.api.FileRepository
import com.mini.ide.data.api.ProjectRepository
import com.mini.ide.data.api.SettingsRepository
import com.mini.ide.data.api.TemplateRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindsModule {
    @Binds
    @Singleton
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository

    @Binds
    @Singleton
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository

    @Binds
    @Singleton
    abstract fun bindTemplateRepository(impl: TemplateRepositoryImpl): TemplateRepository

    @Binds
    @Singleton
    abstract fun bindContentStore(impl: DbContentStore): ContentStore
}

@Module
@InstallIn(SingletonComponent::class)
object DataProvidesModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MiniMeDatabase =
        MiniMeDatabase.build(context)

    @Provides
    @Singleton
    fun provideFileDao(db: MiniMeDatabase): FileDao = db.fileDao()

    @Provides
    @Singleton
    fun provideDbContentStore(dao: FileDao): DbContentStore = DbContentStore(dao)

    @Provides
    @Singleton
    fun provideTemplateRepositoryImpl(db: MiniMeDatabase): TemplateRepositoryImpl =
        TemplateRepositoryImpl(db)

    @Provides
    @Singleton
    fun provideFileRepositoryImpl(db: MiniMeDatabase, contentStore: ContentStore): FileRepositoryImpl =
        FileRepositoryImpl(db, contentStore)

    @Provides
    @Singleton
    fun provideProjectRepositoryImpl(
        db: MiniMeDatabase,
        templates: TemplateRepository,
    ): ProjectRepositoryImpl = ProjectRepositoryImpl(db, templates)

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository =
        SettingsRepositoryImpl(context)
}
