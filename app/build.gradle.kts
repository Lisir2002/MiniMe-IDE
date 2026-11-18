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

// 正式签名：凭据来自环境变量（云端由 GitHub Actions Secrets 注入）。
// keystore 二进制不入库（.gitignore 排除），本地开发无这些变量时 release buildType
// 回退到 debug 签名，保证本地单测/编译冒烟不受影响；云端发版必须注入完整变量。
val signingStoreFile = System.getenv("SIGNING_KEYSTORE_FILE")?.let { rootProject.file(it) }
val signingStorePassword = System.getenv("SIGNING_STORE_PASSWORD")
val signingKeyAlias = System.getenv("SIGNING_KEY_ALIAS")
val signingKeyPassword = System.getenv("SIGNING_KEY_PASSWORD")
val hasReleaseSigning = signingStoreFile?.exists() == true &&
    !signingStorePassword.isNullOrBlank() &&
    !signingKeyAlias.isNullOrBlank() &&
    !signingKeyPassword.isNullOrBlank()

android {
    namespace = "com.mini.ide"
    compileSdk = 36

    // AGP 8 默认关闭 BuildConfig 生成；AppStartup 需读取 VERSION_NAME 写启动日志。
    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = signingStoreFile
                storePassword = signingStorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

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
            // R8：代码混淆 + 资源压缩，测试版/正式版共用 release buildType，均开混淆。
            isMinifyEnabled = true
            isShrinkResources = true
            // 统一正式签名：云端注入 Secrets 后用固定 keystore（所有 rc/正式版同一把钥匙，
            // 覆盖安装不再冲突）；本地无凭据时回退 debug 签名，不影响单测/编译冒烟。
            signingConfig = if (hasReleaseSigning)
                signingConfigs.getByName("release")
            else
                signingConfigs.getByName("debug")
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
    implementation(project(":core:log"))
    implementation(project(":core:permission"))
    implementation(project(":data:api"))
    implementation(project(":data:impl"))
    implementation(project(":engine:api"))
    implementation(project(":engine:impl"))
    implementation(project(":language:api"))
    implementation(project(":language:dsl"))
    implementation(project(":language:impl"))
    implementation(project(":feature:project"))
    implementation(project(":feature:editor"))
    implementation(project(":feature:preview"))
    implementation(project(":feature:lesson"))
    implementation(project(":extension:api"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
}
