plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
}

dependencies {
    implementation(project(":data:api"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
}
