plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

dependencies {
    api(project(":core:model"))
    api(project(":language:api"))
    api(libs.coroutines.core)
}
