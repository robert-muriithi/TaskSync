plugins {
    alias(libs.plugins.tasksync.android.library)
    alias(libs.plugins.tasksync.android.hilt)
}

android {
    namespace = "com.dlight.domain"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.bundles.testing)
}