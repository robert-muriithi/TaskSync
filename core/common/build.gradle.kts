plugins {
    alias(libs.plugins.tasksync.android.library)
    alias(libs.plugins.tasksync.android.hilt)
}

android {
    namespace = "com.dlight.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.bundles.testing)
}