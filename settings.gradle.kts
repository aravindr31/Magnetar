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
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "NorthLight"

// App
include(":app")

// Core modules
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:ui")
include(":core:yts")
include(":core:tmdb")
include(":core:streaming")

// Streaming provider implementations
include(":streaming:torrent")
include(":streaming:realdebrid")
include(":streaming:seedr")

// Feature modules
include(":feature:browse")
include(":feature:details")
include(":feature:player")
include(":feature:settings")
