package com.mini.ide.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/** Hilt 约定插件：应用 Hilt 插件 + KSP。 */
class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.dagger.hilt.android")
        pluginManager.apply("com.google.devtools.ksp")
    }
}
