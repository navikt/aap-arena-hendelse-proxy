import org.gradle.api.initialization.resolve.RepositoriesMode

rootProject.name = "build-logic"

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }

    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}
