package com.mini.ide.language.impl

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LanguageModule {
    @Provides
    @Singleton
    fun provideRegistry(): LanguageRegistry = LanguageRegistry()
}
