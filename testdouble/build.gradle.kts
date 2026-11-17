plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

dependencies {
    api(project(":core:model"))
    api(project(":data:api"))
    api(project(":engine:api"))
    api(project(":language:api"))
    api(libs.coroutines.core)
}
