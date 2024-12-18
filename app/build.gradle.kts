import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.1.0"
    id("io.ktor.plugin") version "3.0.2"
    application
}

val aapLibVersion = "5.0.25"
val ktorVersion = "3.0.2"

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

application {
    mainClass.set("proxy.AppKt")
}

dependencies {
    implementation("com.github.navikt.aap-libs:ktor-auth:$aapLibVersion")
    implementation("com.github.navikt.aap-libs:kafka:$aapLibVersion")
    implementation("org.apache.kafka:kafka-clients:3.9.0")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    constraints {
        implementation("io.netty:netty-common:4.1.115.Final")
    }
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")


    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")

    implementation("io.micrometer:micrometer-registry-prometheus:1.14.1")

    runtimeOnly("net.logstash.logback:logstash-logback-encoder:8.0")

    testImplementation(kotlin("test"))

    testImplementation("org.assertj:assertj-core:3.26.3")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        mergeServiceFiles()
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDirs("main")
sourceSets["test"].resources.srcDirs("test")
