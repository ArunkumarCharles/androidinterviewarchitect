// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.dagger.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt)
}

subprojects {
    tasks.withType<Test> {
        systemProperty("net.bytebuddy.experimental", "true")
    }
}

// One detekt run over every module's Kotlin sources. Existing findings live in detekt-baseline.xml so CI
// fails only on *new* issues; shrink the baseline as code is cleaned up.
detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("config/detekt/detekt.yml"))
    baseline = file("config/detekt/baseline.xml")
    source.setFrom(files(subprojects.map { "${it.projectDir}/src" }))
    parallel = true
}
