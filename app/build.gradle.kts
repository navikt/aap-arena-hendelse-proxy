import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    id("aap.conventions")
    kotlin("jvm")
    id("io.ktor.plugin") version "3.3.3"
    application
}

val komponenterVersjon = "1.0.936"
val ktorVersion = "3.3.3"
val mockOAuth2ServerVersion = "3.0.1"
val testcontainersVersion = "2.0.3"

application {
    mainClass.set("no.nav.aap.proxy.AppKt")
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:4.1.1")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("no.nav.aap.kelvin:server:$komponenterVersjon")
    implementation("no.nav.aap.kelvin:infrastructure:$komponenterVersjon")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.20.1")

    implementation("io.micrometer:micrometer-registry-prometheus:1.16.1")

    implementation("ch.qos.logback:logback-classic:1.5.24")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:9.0")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOAuth2ServerVersion")
    constraints {
        implementation("net.minidev:json-smart:2.6.0")
    }
    testImplementation("org.assertj:assertj-core:3.27.7")

    // Testcontainers
    testImplementation("org.testcontainers:testcontainers-postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:${testcontainersVersion}")
    testImplementation("org.testcontainers:testcontainers-kafka:$testcontainersVersion")
}

tasks {
    withType<ShadowJar> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        mergeServiceFiles()
    }
}