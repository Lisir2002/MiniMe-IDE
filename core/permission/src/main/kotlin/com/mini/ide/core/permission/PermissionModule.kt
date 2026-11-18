package com.mini.ide.core.permission

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PermissionModule {
    @Binds
    @Singleton
    abstract fun bindPermissionChecker(impl: AndroidPermissionChecker): PermissionChecker
}

@Module
@InstallIn(SingletonComponent::class)
object PermissionProvidesModule {
    @Provides
    @Singleton
    fun providePermissionManager(checker: PermissionChecker): PermissionManager =
        PermissionManager(checker)
}
