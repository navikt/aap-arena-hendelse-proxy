import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    includeBuild("build-logic")
}

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "arena-hendelse-proxy"
include("app")


dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    // Felles for alle gradle prosjekter i repoet
    versionCatalogs {
        create("kelvinLibs") {
            from("no.nav.aap.kelvin:version-catalog:2.0.186")
        }
    }

    @Suppress("UnstableApiUsage")
    repositories {
        exclusiveContent {
            forRepository {
                maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
            }
            filter {
                includeGroupByRegex("no\\.nav(\\..*)?")
            }
        }
        mavenCentral()
    }
}
