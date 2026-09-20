import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Compose setup: the Kotlin Compose compiler plugin (bundled with Kotlin 2.0+, so no separate
 * `composeOptions.kotlinCompilerExtensionVersion` to keep in sync), the BOM for aligned Compose versions,
 * and the UI artifacts every screen uses.
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        // Look up by name: AGP registers the concrete type (application/library), not CommonExtension itself,
        // so a by-type lookup of the shared supertype finds nothing.
        (extensions.getByName("android") as CommonExtension).buildFeatures.compose = true
        dependencies {
            add("implementation", platform(libs.findLibrary("androidx-compose-bom").get()))
            add("implementation", libs.findBundle("androidx-compose").get())
        }
    }
}
