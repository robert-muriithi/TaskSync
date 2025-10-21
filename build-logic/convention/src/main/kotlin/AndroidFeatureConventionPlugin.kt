import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("tasksync.android.library")
                apply("tasksync.android.hilt")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }
            }

            val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                val composeBom = libs.findLibrary("androidx-compose-bom").get()
                add(configurationName = "implementation", dependencyNotation = platform(composeBom))

                libs.findBundle("compose").ifPresent { bundle ->
                    add(configurationName = "implementation", dependencyNotation = bundle)
                }
                
                libs.findBundle("compose-debug").ifPresent { bundle ->
                    add(configurationName = "debugImplementation", dependencyNotation = bundle)
                }
                
                libs.findBundle("lifecycle").ifPresent { bundle ->
                    add(configurationName = "implementation", dependencyNotation = bundle)
                }
                
                add(
                    configurationName = "implementation",
                    dependencyNotation = libs.findLibrary("androidx-navigation-compose").get()
                )
                add(
                    configurationName = "implementation",
                    dependencyNotation = libs.findLibrary("hilt-navigation-compose").get()
                )
                
                add(
                    configurationName = "androidTestImplementation",
                    dependencyNotation = libs.findLibrary("androidx-compose-ui-test-junit4").get()
                )
            }
        }
    }
}