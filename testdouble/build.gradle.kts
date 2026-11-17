plugins {
    alias(libs.plugins.android.library)
}

dependencies {
    implementation(project(":data:api"))
    implementation(project(":engine:api"))
    implementation(project(":core:model"))
}
