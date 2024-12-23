import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.1.0"
    id("io.ktor.plugin") version "3.0.3"
    application
}

val aapLibVersion = "5.0.25"
val komponenterVersjon = "1.0.101"
val ktorVersion = "3.0.3"

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

application {
    mainClass.set("proxy.AppKt")
}

dependencies {
    implementation("com.github.navikt.aap-libs:kafka:$aapLibVersion")
    implementation("org.apache.kafka:kafka-clients:3.9.0")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("no.nav.aap.kelvin:server:$komponenterVersjon")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")

    implementation("io.micrometer:micrometer-registry-prometheus:1.14.1")


    implementation("ch.qos.logback:logback-classic:1.5.12")
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
