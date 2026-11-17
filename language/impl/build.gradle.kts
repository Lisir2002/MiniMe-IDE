plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
}

dependencies {
    implementation(project(":language:api"))
    implementation(project(":core:common"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
}
