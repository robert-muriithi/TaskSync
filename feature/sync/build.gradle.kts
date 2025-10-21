plugins {
    alias(libs.plugins.tasksync.android.library)
    alias(libs.plugins.tasksync.android.hilt)
}

android {
    namespace = "com.dlight.sync"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:common"))
    
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.androidx.compiler)
    
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.androidx.work.testing)
}