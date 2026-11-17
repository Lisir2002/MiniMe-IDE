plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
}

android {
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":engine:api"))
    implementation(project(":data:api"))
    implementation(project(":language:api"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:uicommon"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
}
