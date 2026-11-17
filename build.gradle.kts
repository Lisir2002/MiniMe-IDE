plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

/**
 * 架构铁律校验：扫描每个模块的 build.gradle.kts 中的 project(":...") 依赖，
 * 违反规则则构建失败。挂到 check，保证 ./gradlew build 时自动执行。
 */
val projectDepRegex = Regex("""project\(\s*"([^"]+)"\s*\)""")

fun violatedRule(from: String, to: String): String? {
    if (to == ":app") return "任何模块不得依赖 :app"
    if (from.startsWith(":core:") && listOf(":data", ":engine", ":language", ":feature").any { to.startsWith(it) })
        return "core 层不得依赖业务模块"
    if (from.endsWith(":api") && to.endsWith(":impl")) return "api 模块不得依赖 impl"
    if (from.startsWith(":feature:") && to.endsWith(":impl")) return "feature 不得依赖 impl（只能依赖 api）"
    if (from.endsWith(":impl") && to.startsWith(":feature:")) return "impl 不得依赖 feature"
    return null
}

tasks.register("checkModuleRules") {
    group = "verification"
    description = "校验模块依赖规则（架构铁律）"
    doLast {
        val errors = mutableListOf<String>()
        var moduleCount = 0
        subprojects.forEach { p ->
            val file = p.file("build.gradle.kts")
            if (!file.exists()) return@forEach
            moduleCount++
            val content = file.readText()
            projectDepRegex.findAll(content).forEach { match ->
                val dep = match.groupValues[1]
                violatedRule(p.path, dep)?.let { errors.add("${p.path} 违规依赖 $dep → $it") }
            }
        }
        if (errors.isNotEmpty()) {
            throw GradleException("依赖规则校验失败（${errors.size} 处）：\n  " + errors.joinToString("\n  "))
        }
        println("✔ 依赖规则校验通过：$moduleCount 个模块")
    }
}

// 统一配置：所有 android.library 模块的 SDK 与 Java 版本（配置收敛，避免逐模块漂移）
subprojects {
    plugins.withId("com.android.library") {
        extensions.configure<com.android.build.gradle.LibraryExtension> {
            // 命名空间按模块路径推导：:core:common -> com.mini.ide.core.common
            namespace = "com.mini.ide" + path.replace(':', '.')
            compileSdk = 36
            defaultConfig { minSdk = 31 }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
        // 让 build 触发架构铁律校验
        tasks.named("check") { dependsOn(rootProject.tasks.named("checkModuleRules")) }
    }
}
