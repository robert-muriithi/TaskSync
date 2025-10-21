import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                compileSdk = 36

                defaultConfig {
                    minSdk = 24
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                    isCoreLibraryDesugaringEnabled = true
                }
            }
            tasks.withType<KotlinCompile>().configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }

            val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                add(
                    configurationName = "implementation",
                    dependencyNotation = libs.findLibrary("androidx-core-ktx").get()
                )
                add(
                    configurationName = "implementation",
                    dependencyNotation = libs.findLibrary("timber").get()
                )
                add(
                    configurationName = "coreLibraryDesugaring",
                    dependencyNotation = libs.findLibrary("desugar-jdk-libs").get()
                )
                add(
                    configurationName = "testImplementation",
                    dependencyNotation = libs.findLibrary("junit").get()
                )
                add(
                    configurationName = "androidTestImplementation",
                    dependencyNotation = libs.findLibrary("androidx-junit").get()
                )
                add(
                    configurationName = "androidTestImplementation",
                    dependencyNotation = libs.findLibrary("androidx-espresso-core").get()
                )
            }
        }
    }
}