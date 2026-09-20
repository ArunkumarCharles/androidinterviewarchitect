import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * A feature module is a library + Hilt + Compose that may depend on `:domain` and `:core:model` and nothing
 * else in the app. Encoding that here means a feature cannot accidentally gain a dependency on `:core:data` or
 * another feature just by copy-pasting a build file.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("architect.android.library")
        pluginManager.apply("architect.android.hilt")
        pluginManager.apply("architect.android.compose")
        dependencies {
            add("implementation", project(":core:model"))
            add("implementation", project(":domain"))
            add("implementation", libs.findBundle("androidx-lifecycle-compose").get())
            add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
            add("testImplementation", libs.findBundle("unit-test").get())
        }
    }
}
