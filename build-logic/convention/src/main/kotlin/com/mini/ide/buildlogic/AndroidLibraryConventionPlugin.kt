package com.mini.ide.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import com.android.build.gradle.LibraryExtension

/**
 * 所有 Android Library 模块的统一约定：
 *  - compileSdk / minSdk = 31（Android 12，磨砂玻璃 RenderEffect 门槛）
 *  - Java 17
 *  - 统一注册 checkModuleRules 依赖规则任务
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")

        extensions.configure<LibraryExtension> {
            compileSdk = 36
            defaultConfig {
                minSdk = 31
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
                targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
            }
        }

        // 注册依赖规则校验，并挂到 check 生命周期
        ModuleRulesTask.register(this)
        tasks.named("check") { dependsOn(ModuleRulesTask.TASK_NAME) }
    }
}
