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
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FD-Tracker"

include(":app")
include(":core:data")
include(":core:domain")
include(":core:ui")
include(":core:common")
include(":feature:dashboard")
include(":feature:fdlist")
include(":feature:fddetail")
include(":feature:calendar")
include(":feature:ocr")
include(":feature:tax")
include(":feature:strategy")
include(":feature:settings")
