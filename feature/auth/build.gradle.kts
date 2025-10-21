plugins {
    alias(libs.plugins.tasksync.android.feature)
}

android {
    namespace = "com.dlight.auth"
}

dependencies {
    implementation(project(":core:ui"))
    api(project(":core:domain"))
}