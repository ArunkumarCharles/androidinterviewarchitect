plugins {
    id("architect.android.library")
    id("architect.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.sevvanam.android_interview_architect.core.network"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
}
