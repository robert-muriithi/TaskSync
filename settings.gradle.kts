pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

includeBuild("build-logic")

rootProject.name = "TaskSync - d.light"
include(":app")
include(":core")
include(":core:common")
include(":core:database")
include(":core:network")
include(":core:domain")
include(":feature")
include(":feature:auth")
include(":feature:tasks")
include(":feature:sync")
include(":core:data")
include(":core:ui")
