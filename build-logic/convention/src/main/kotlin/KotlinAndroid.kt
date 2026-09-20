import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

/**
 * The Android settings every module shares. Centralised so a change (e.g. raising compileSdk or the JVM
 * target) is a one-line edit instead of an edit in eleven files that can silently drift apart.
 *
 * With AGP 9's built-in Kotlin the Kotlin JVM target is derived from `compileOptions`, so Java and Kotlin can no
 * longer disagree (the old "inconsistent JVM-target compatibility" error).
 */
internal fun Project.configureKotlinAndroid(extension: CommonExtension) {
    extension.apply {
        compileSdk = libs.version("compileSdk").toInt()
        defaultConfig.minSdk = libs.version("minSdk").toInt()
        // Property access, not the `compileOptions { }` block: in AGP 9 the block only exists on the concrete
        // Application/Library extensions, while the property is on the shared CommonExtension.
        compileOptions.sourceCompatibility = JavaVersion.VERSION_17
        compileOptions.targetCompatibility = JavaVersion.VERSION_17
    }
}
