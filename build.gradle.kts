plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "dev.westelh"
version = "0.0.5"

application {
    mainClass.set("dev.westelh.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.sessions)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.serialization.properties)
    implementation(libs.kotlinx.datetime)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.server.html.builder)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.logging)
    implementation("com.nimbusds:nimbus-jose-jwt:10.2")
    testImplementation(libs.ktor.client.test)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.assertions.ktor)
}

tasks.compileTestKotlin {
    compilerOptions.freeCompilerArgs.add("-Xdebug")
}