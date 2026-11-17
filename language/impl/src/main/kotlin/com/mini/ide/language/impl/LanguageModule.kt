package com.mini.ide.language.impl

import com.mini.ide.language.api.LanguageProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LanguageBindsModule {
    @Binds
    @Singleton
    abstract fun bindLanguageProvider(impl: LanguageRegistry): LanguageProvider
}

@Module
@InstallIn(SingletonComponent::class)
object LanguageModule {
    @Provides
    @Singleton
    fun provideRegistry(): LanguageRegistry = LanguageRegistry()
}
