import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    id("aap.conventions")
    kotlin("jvm")
    alias(kelvinLibs.plugins.ktor)
    application
}

application {
    mainClass.set("no.nav.aap.proxy.AppKt")
}

dependencies {
    implementation(kelvinLibs.kafka.clients)

    implementation(kelvinLibs.ktor.server.core)
    implementation(kelvinLibs.ktor.server.status.pages)
    implementation(libs.server)
    implementation(libs.infrastructure)

    implementation(kelvinLibs.jackson.datatype.jsr310)

    implementation(kelvinLibs.micrometer.prometheus)

    implementation(kelvinLibs.logback.classic)
    runtimeOnly(kelvinLibs.logstash.logback.encoder)

    testImplementation(kelvinLibs.ktor.server.test.host)
    testImplementation(kelvinLibs.ktor.client.content.negotiation)
    testImplementation(kelvinLibs.mock.oauth2.server)
    testImplementation(kelvinLibs.bundles.junit)

    testImplementation(kelvinLibs.testcontainers.postgresql)
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:${kelvinLibs.versions.testcontainers.get()}")
    testImplementation(kelvinLibs.testcontainers.kafka)
}

tasks {
    withType<ShadowJar> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        mergeServiceFiles()
    }
}