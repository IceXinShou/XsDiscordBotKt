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
    compileOnly(project(":Plugins:Placeholder"))
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("Plugins/MessageCreator/src/main/kotlin"))
        }
    }
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("MessageCreator-${properties["prefix"]}")
    archiveVersion.set("$version")
    archiveClassifier.set("")
    destinationDirectory.set(file("../../Server/plugins"))

    dependencies {
    }
}

kotlin {
    jvmToolchain(21)
}