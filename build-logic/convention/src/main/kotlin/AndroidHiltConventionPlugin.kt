import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("com.google.dagger.hilt.android")
            }

            val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
            dependencies {
                add(
                    configurationName = "implementation",
                    dependencyNotation = libs.findLibrary("hilt-android").get()
                )
                add(
                    configurationName = "ksp",
                    dependencyNotation = libs.findLibrary("hilt-compiler").get()
                )
                add(
                    configurationName = "testImplementation",
                    dependencyNotation = libs.findLibrary("hilt-android-testing").get()
                )
                add(
                    configurationName = "kspTest",
                    dependencyNotation = libs.findLibrary("hilt-compiler").get()
                )
                add(
                    configurationName = "androidTestImplementation",
                    dependencyNotation = libs.findLibrary("hilt-android-testing").get()
                )
                add(
                    configurationName = "kspAndroidTest",
                    dependencyNotation = libs.findLibrary("hilt-compiler").get()
                )
            }
        }
    }
}