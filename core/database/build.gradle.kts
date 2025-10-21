plugins {
    alias(libs.plugins.tasksync.android.library)
    alias(libs.plugins.tasksync.android.room)
    alias(libs.plugins.tasksync.android.hilt)
}

android {
    namespace = "com.dlight.database"

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
    androidTestImplementation(libs.bundles.testing)
    androidTestImplementation(libs.androidx.room.testing)
}