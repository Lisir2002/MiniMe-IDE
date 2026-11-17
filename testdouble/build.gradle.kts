plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

dependencies {
    implementation(project(":data:api"))
    implementation(project(":engine:api"))
    implementation(project(":core:model"))
}
