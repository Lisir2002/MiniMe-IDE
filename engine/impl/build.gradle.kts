plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
}

dependencies {
    implementation(project(":engine:api"))
    implementation(project(":data:api"))
    implementation(project(":language:api"))
    implementation(project(":core:common"))
    implementation(libs.coroutines.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
}
