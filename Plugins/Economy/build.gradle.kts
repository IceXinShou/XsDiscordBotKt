group = "tw.xserver.plugin"
version = "v2.0"

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20-RC"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":"))
    compileOnly(project(":Plugins:MessageCreator"))
    compileOnly(project(":Plugins:Placeholder"))
    compileOnly(project(":Plugins:API:GoogleSheetAPI"))
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    compileOnly("com.google.api-client:google-api-client:2.6.0")
    compileOnly("com.google.oauth-client:google-oauth-client-jetty:1.36.0")
    compileOnly("com.google.apis:google-api-services-sheets:v4-rev20240514-2.0.0")
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("Plugins/Economy/src/main/kotlin"))
        }
    }
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("Economy-${properties["prefix"]}")
    archiveVersion.set("$version")
    archiveClassifier.set("")
    destinationDirectory.set(file("../../Server/plugins"))

    dependencies {
    }
}

kotlin {
    jvmToolchain(21)
}
