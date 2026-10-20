pluginManagement {
    repositories {
        google()
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

rootProject.name = "NovelForge"

include(":app")

include(":core:database")
include(":core:designsystem")
include(":core:model")
include(":core:presentation")
include(":domain:analytics")
include(":domain:backup")
include(":domain:preferences")
include(":domain:story")
include(":domain:workspace")
include(":domain:writing")
include(":feature:analytics")
include(":feature:backup")
include(":feature:editor")
include(":feature:guide")
include(":feature:library")
include(":feature:reader")
include(":feature:settings")
include(":feature:workspace")

include(":data:local")
