// build-logic is an *included build*: Gradle compiles it first and makes its convention plugins available to
// every module in the main build. That is what lets modules say `id("architect.android.feature")` instead of
// repeating ~25 lines of identical Android/Kotlin/Hilt setup.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        // Share the app's catalog so SDK levels, versions and libraries are defined exactly once.
        create("libs") { from(files("../gradle/libs.versions.toml")) }
    }
}

rootProject.name = "build-logic"
include(":convention")
