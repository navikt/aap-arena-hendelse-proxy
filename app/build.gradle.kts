import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    id("aap-hendelse-proxy.conventions")
    kotlin("jvm")
    id("io.ktor.plugin") version "3.1.1"
    application
}

val komponenterVersjon = "1.0.190"
val ktorVersion = "3.1.2"
val mockOAuth2ServerVersion = "2.1.10"
val testcontainersVersion = "1.20.6"

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

application {
    mainClass.set("no.nav.aap.proxy.AppKt")
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:4.0.0")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("no.nav.aap.kelvin:server:$komponenterVersjon")
    implementation("no.nav.aap.kelvin:infrastructure:$komponenterVersjon")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.3")

    implementation("io.micrometer:micrometer-registry-prometheus:1.14.5")

    implementation("ch.qos.logback:logback-classic:1.5.18")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:8.0")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOAuth2ServerVersion")
    constraints {
        implementation("net.minidev:json-smart:2.5.2")
    }
    testImplementation("org.assertj:assertj-core:3.27.3")

    // Testcontainers
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    constraints {
        implementation("org.apache.commons:commons-compress:1.27.1") {
            because("https://github.com/advisories/GHSA-4g9r-vxhx-9pgx")
        }
    }
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:kafka:$testcontainersVersion")
}

tasks {
    withType<ShadowJar> {
        mergeServiceFiles()
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
