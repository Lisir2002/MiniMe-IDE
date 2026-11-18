package com.mini.ide.language.dsl

import com.mini.ide.language.api.DslCompiler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** M6：把自研语言编译器实现绑定为契约接口，engine:impl 只依赖 language:api。 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DslModule {
    @Binds
    @Singleton
    abstract fun bindDslCompiler(impl: DslCompilerImpl): DslCompiler
}
