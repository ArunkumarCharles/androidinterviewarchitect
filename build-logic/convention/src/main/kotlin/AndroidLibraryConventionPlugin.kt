import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Base for every Android library module: AGP library + shared SDK/JVM configuration.
 * AGP 9 compiles Kotlin itself ("built-in Kotlin"), so the separate kotlin-android plugin is no longer applied.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        extensions.configure<LibraryExtension> { configureKotlinAndroid(this) }
    }
}
