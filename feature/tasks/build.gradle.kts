plugins {
    alias(libs.plugins.tasksync.android.feature)
}

android {
    namespace = "com.dlight.tasks"
}

dependencies {
    implementation(project(":core:ui"))
    api(project(":core:domain"))
    implementation(project(":feature:sync"))
}