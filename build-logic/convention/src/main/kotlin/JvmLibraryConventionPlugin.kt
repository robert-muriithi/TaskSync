import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.jvm")
            }

            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }

            val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
            dependencies {
                add(
                    configurationName = "implementation",
                    dependencyNotation = libs.findLibrary("kotlinx-coroutines-android").get()
                )
                add(
                    configurationName = "implementation",
                    dependencyNotation = libs.findLibrary("hilt-android").get()
                )
                add(
                    configurationName = "testImplementation",
                    dependencyNotation = libs.findLibrary("junit").get()
                )
                add(
                    configurationName = "testImplementation",
                    dependencyNotation = libs.findLibrary("kotlinx-coroutines-test").get()
                )
            }
        }
    }
}