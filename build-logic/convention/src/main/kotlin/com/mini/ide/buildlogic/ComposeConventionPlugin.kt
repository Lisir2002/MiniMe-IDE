package com.mini.ide.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/** Compose 约定插件：应用 Kotlin Compose 编译器插件并启用 compose。 */
class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        // buildFeatures.compose 由各模块自行声明（app/library 类型不同）
    }
}
