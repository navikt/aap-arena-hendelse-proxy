import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    id("aap.conventions")
    kotlin("jvm")
    alias(libs.plugins.ktor)
    application
}

application {
    mainClass.set("no.nav.aap.proxy.AppKt")
}

dependencies {
    implementation(libs.kafkaClients)

    implementation(libs.ktorServerCore)
    implementation(libs.ktorServerStatusPages)
    implementation(libs.server)
    implementation(libs.infrastructure)

    implementation(libs.jacksonDatatypeJsr310)

    implementation(libs.micrometerRegistryPrometheus)

    implementation(libs.logbackClassic)
    runtimeOnly(libs.logstashLogbackEncoder)

    testImplementation(libs.ktorServerTestHost)
    testImplementation(libs.ktorClientContentNegotiation)
    testImplementation(libs.mockOAuth2Server)
    constraints {
        implementation(libs.jsonSmart)
    }
    testImplementation(libs.assertJ)

    // Testcontainers
    testImplementation(libs.testcontainersPostgres)
    testImplementation(libs.testcontainersJunitJupiter)
    testImplementation(libs.testcontainersKafka)
}

tasks {
    withType<ShadowJar> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        mergeServiceFiles()
    }
}