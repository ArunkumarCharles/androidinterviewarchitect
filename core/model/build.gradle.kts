plugins {
    id("architect.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.sevvanam.android_interview_architect.core.model"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
