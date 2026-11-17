package com.mini.ide.di

import com.mini.ide.core.common.DefaultDispatcherProvider
import com.mini.ide.core.common.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 全局装配骨架。core:common 无 Hilt 插件，DefaultDispatcherProvider 用 @Provides 构造。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider
}

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {
    @Provides
    @Singleton
    fun provideDefaultDispatcherProvider(): DefaultDispatcherProvider = DefaultDispatcherProvider()
}
