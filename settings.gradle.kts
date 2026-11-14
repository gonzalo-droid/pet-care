rootProject.name = "BestID"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
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
include(":composeApp")
include(":core:model")
include(":core:domain")
include(":core:data")
include(":core:platform")
include(":core:designsystem")
include(":core:ui")
include(":core:navigation")
include(":feature:home")
include(":feature:cartilla")
include(":feature:health")
include(":feature:pet")
include(":feature:onboarding")
include(":feature:share")
include(":feature:settings")
