import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = "tw.xserver.plugin.api"
version = "v2.0"

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20-RC"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.google.api-client:google-api-client:2.6.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.36.0")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20240514-2.0.0")
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("Plugins/API/GoogleSheetAPI/src/main/kotlin"))
        }
    }
}


tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("GoogleSheetAPI-${properties["prefix"]}")
    archiveVersion.set("$version")
    archiveClassifier.set("")
    destinationDirectory.set(file("../../../Server/plugins"))
    configurations = listOf(project.configurations.getByName("runtimeClasspath"))
}

kotlin {
    jvmToolchain(21)
}