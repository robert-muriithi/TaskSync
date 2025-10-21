plugins {
    alias(libs.plugins.tasksync.android.library)
    alias(libs.plugins.tasksync.android.hilt)
}

android {
    namespace = "com.dlight.network"
}

dependencies {
    implementation(libs.bundles.retrofit)
    implementation(libs.kotlinx.coroutines.android)
    
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
}