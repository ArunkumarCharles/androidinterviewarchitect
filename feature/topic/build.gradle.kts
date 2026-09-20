plugins {
    id("architect.android.feature")
}

android {
    namespace = "com.sevvanam.android_interview_architect.feature.topic"
}

dependencies {
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
