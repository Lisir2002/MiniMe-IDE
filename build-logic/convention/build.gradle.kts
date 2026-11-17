plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "com.mini.ide.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.android.tools.build.gradle)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "minimide.android.library"
            implementationClass = "com.mini.ide.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("compose") {
            id = "minimide.compose"
            implementationClass = "com.mini.ide.buildlogic.ComposeConventionPlugin"
        }
        register("hilt") {
            id = "minimide.hilt"
            implementationClass = "com.mini.ide.buildlogic.HiltConventionPlugin"
        }
    }
}
