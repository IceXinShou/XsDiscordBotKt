import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val pluginName = "GoogleSheetAPI"
group = "tw.xserver.plugin.api"
version = "v2.0"

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20-RC"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.google.api-client:google-api-client:2.6.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.36.0")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20240514-2.0.0")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.named<ShadowJar>("shadowJar") {
    val outputPath: File by rootProject.extra
    archiveBaseName = pluginName
    archiveAppendix = "${properties["prefix"]}"
    archiveVersion = "$version"
    archiveClassifier = ""
    archiveExtension = "jar"
    destinationDirectory = outputPath.resolve("plugins")
}
