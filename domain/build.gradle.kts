plugins {
    id("architect.android.library")
    id("architect.android.hilt")
}

android {
    namespace = "com.sevvanam.android_interview_architect.domain"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
    api(libs.paging.common)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
}
