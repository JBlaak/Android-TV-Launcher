import dev.jblaak.tvlauncher.configureKotlinJvm
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
                apply("com.android.lint")
            }
            configureKotlinJvm()

            dependencies {
                add("testImplementation", kotlin("test-junit"))
            }
        }
    }
}
