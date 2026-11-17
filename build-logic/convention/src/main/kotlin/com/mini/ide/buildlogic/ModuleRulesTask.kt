package com.mini.ide.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

/**
 * 依赖规则校验任务：把架构铁律变成编译期约束。
 *
 * 规则（按模块路径前缀判定）：
 *  - :core:*   不得依赖任何业务模块（data/engine/language/feature）
 *  - :*:api    不得依赖任何 :*:impl
 *  - :feature:*不得依赖任何 :*:impl（只能依赖 api，实现经 Hilt 注入）
 *  - :data:impl / :engine:impl / :language:impl 不得依赖 :feature:*
 *  - 任何模块不得依赖 :app
 *
 * 通过扫描各模块 build.gradle.kts 中的 project(":...") 声明实现，
 * 简单、确定、无需解析依赖图，适合脚手架阶段。
 */
abstract class ModuleRulesTask : DefaultTask() {

    @TaskAction
    fun check() {
        val root = project.rootProject
        val errors = mutableListOf<String>()

        // 收集所有模块 build.gradle.kts
        val buildFiles = root.allprojects
            .filter { it != root }
            .mapNotNull { p ->
                val f = p.file("build.gradle.kts")
                if (f.exists()) p.path to f.readText() else null
            }

        for ((path, content) in buildFiles) {
            val deps = PROJECT_DEP_REGEX.findAll(content)
                .map { it.groupValues[1] }
                .toList()

            for (dep in deps) {
                val rule = violatedRule(path, dep)
                if (rule != null) {
                    errors.add("$path 违规依赖 $dep → $rule")
                }
            }
        }

        if (errors.isNotEmpty()) {
            throw GradleException(
                "依赖规则校验失败（${errors.size} 处）：\n" + errors.joinToString("\n  ") + "\n"
            )
        }
        logger.lifecycle("✔ 依赖规则校验通过：${buildFiles.size} 个模块")
    }

    private fun violatedRule(from: String, to: String): String? {
        if (to == ":app") return "任何模块不得依赖 :app（装配层例外仅 app 自身）"
        if (from.startsWith(":core:") && isBusiness(to)) {
            return "core 层不得依赖业务模块"
        }
        if (from.endsWith(":api") && to.endsWith(":impl")) {
            return "api 模块不得依赖 impl"
        }
        if (from.startsWith(":feature:") && to.endsWith(":impl")) {
            return "feature 不得依赖 impl（只能依赖 api）"
        }
        if (from.endsWith(":impl") && to.startsWith(":feature:")) {
            return "impl 不得依赖 feature"
        }
        return null
    }

    private fun isBusiness(path: String): Boolean =
        listOf(":data", ":engine", ":language", ":feature").any { path.startsWith(it) }

    companion object {
        const val TASK_NAME = "checkModuleRules"
        private val PROJECT_DEP_REGEX = Regex("""project\(\s*"([^"]+)"\s*\)""")

        fun register(project: Project): TaskProvider<ModuleRulesTask> =
            project.tasks.register(TASK_NAME, ModuleRulesTask::class.java) {
                group = "verification"
                description = "校验模块依赖规则（架构铁律）"
            }
    }
}
