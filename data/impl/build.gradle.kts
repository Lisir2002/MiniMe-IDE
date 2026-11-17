plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true   // Robolectric 需要资源
            all { it.systemProperty("robolectric.logging", "stdout") }
        }
    }
    // Room schema 导出：数据库结构变更可审计、可写迁移测试
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
    }
}

dependencies {
    implementation(project(":data:api"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // 本地 JVM 单测：Robolectric 让 Room 在 JVM 上真实运行
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.coroutines.core)
}
