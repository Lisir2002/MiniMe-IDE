plugins {
    alias(libs.plugins.android.library)
}

dependencies {
    api(project(":core:model"))
    implementation(libs.coroutines.core)
}
