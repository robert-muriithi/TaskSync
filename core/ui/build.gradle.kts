plugins {
    alias(libs.plugins.tasksync.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.dlight.ui"
    
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.activity.compose)
    
    implementation(libs.bundles.lifecycle)
    
    debugImplementation(libs.bundles.compose.debug)
}