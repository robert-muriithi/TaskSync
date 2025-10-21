plugins {
    alias(libs.plugins.tasksync.android.library)
    alias(libs.plugins.tasksync.android.hilt)
}

android {
    namespace = "com.dlight.data"

    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md"
            )
        }
    }
}

dependencies {
    api(project(":core:domain"))
    api(project(":core:network"))
    implementation(project(":core:database"))
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.bundles.retrofit)

    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.mockwebserver.v521)
    androidTestImplementation(libs.bundles.testing)
    androidTestImplementation(libs.androidx.room.testing)
}