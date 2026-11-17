package com.mini.ide.di

import com.mini.ide.core.common.DefaultDispatcherProvider
import com.mini.ide.core.common.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 全局装配骨架。M0 只绑定 DispatcherProvider 作为示范；
 * 其余接口↔实现绑定在 M1–M3 各实现就位后逐步加入。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider
}
