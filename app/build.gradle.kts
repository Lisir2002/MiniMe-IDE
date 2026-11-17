import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// 版本真源：version.properties
val versionProps = Properties().apply {
    rootProject.file("version.properties").inputStream().use { load(it) }
}
val versionMajor = versionProps.getProperty("VERSION_MAJOR").toInt()
val versionMinor = versionProps.getProperty("VERSION_MINOR").toInt()
val versionPatch = versionProps.getProperty("VERSION_PATCH").toInt()
val rcNumber = versionProps.getProperty("RC_NUMBER").toInt()
val baseVersion = "$versionMajor.$versionMinor.$versionPatch"

val isReleaseBuild = gradle.startParameter.taskNames.any {
    it.contains("Release", ignoreCase = true)
}
val releaseVersionOverride = providers.gradleProperty("releaseVersion").orNull

android {
    namespace = "com.mini.ide"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mini.ide"
        minSdk = 31
        targetSdk = 36
        versionCode = rootProject.file("release/versioncode.txt").readText().trim().toInt()
        versionName = releaseVersionOverride
            ?: if (isReleaseBuild) baseVersion else "$baseVersion-rc$rcNumber"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:uicommon"))
    implementation(project(":data:api"))
    implementation(project(":data:impl"))
    implementation(project(":engine:api"))
    implementation(project(":engine:impl"))
    implementation(project(":language:api"))
    implementation(project(":language:impl"))
    implementation(project(":feature:project"))
    implementation(project(":feature:editor"))
    implementation(project(":feature:preview"))
    implementation(project(":feature:lesson"))
    implementation(project(":extension:api"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
}
