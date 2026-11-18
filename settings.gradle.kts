pluginManagement {
    includeBuild("build-logic")
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

rootProject.name = "MiniMe-IDE"

include(":app")
include(":core:common")
include(":core:model")
include(":core:designsystem")
include(":core:uicommon")
include(":data:api")
include(":data:impl")
include(":engine:api")
include(":engine:impl")
include(":language:api")
include(":language:dsl")
include(":language:impl")
include(":feature:project")
include(":feature:editor")
include(":feature:preview")
include(":feature:lesson")
include(":extension:api")
include(":testdouble")
