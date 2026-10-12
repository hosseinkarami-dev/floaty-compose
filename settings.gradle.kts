rootProject.name = "Floaty"

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when {
                requested.id.id == "org.jetbrains.compose" ->
                    useModule("org.jetbrains.compose:compose-gradle-plugin:${requested.version}")
                requested.id.id.startsWith("com.android.") ->
                    useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
    repositories {
        maven("https://repo.maven.apache.org/maven2")
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":desktopApp")
include(":shared")
include(":floaty")
include(":webApp")
