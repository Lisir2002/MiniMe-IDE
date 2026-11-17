plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":engine:api"))
    implementation(project(":data:api"))
    implementation(project(":language:api"))
    implementation(project(":core:common"))
    implementation(libs.coroutines.android)
    implementation(libs.org.json)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    testImplementation(project(":testdouble"))
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
