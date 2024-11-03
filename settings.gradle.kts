@file:Suppress("UnstableApiUsage", "DEPRECATION")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        // noinspection JcenterRepositoryObsolete
        jcenter()
        maven(url = "https://jitpack.io")
        maven(url = "https://central.maven.org/maven2/")
        maven(url = "https://jitpack.io")
    }
}
rootProject.buildFileName = "project.gradle.kts"

rootProject.name = "BulkYoutubeSearch"
include(":app", ":support")