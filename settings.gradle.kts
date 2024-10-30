pluginManagement {
    // Retrieve Meta Spatial SDK Version from "gradle.properties"
    val metaSpatialSdkVersion: String by settings

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
        maven { url = uri("https://dl.google.com/dl/android/maven2/") }
    }
    plugins {
        id("com.meta.spatial.plugin") version metaSpatialSdkVersion
        id("com.google.devtools.ksp") version "2.0.0-1.0.24"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Torrid"
include(":app")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
