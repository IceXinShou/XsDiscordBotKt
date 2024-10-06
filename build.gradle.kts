import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
    id("com.gradleup.shadow") version "8.3.3"
}

group = "tw.xserver.loader"
version = "v2.0"
java.sourceCompatibility = JavaVersion.VERSION_21

val outputPath = file("${rootProject.projectDir}/Server")
extra["outputPath"] = outputPath

defaultTasks("build")  // Allow to use `./gradlew` to auto build a full project

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:25.0.0")

    api("net.dv8tion:JDA:5.1.2") // JDA
    api("ch.qos.logback:logback-classic:1.5.8") // Log
    api("com.charleskorn.kaml:kaml:0.61.0") // Yaml
    api("com.google.code.gson:gson:2.11.0") // Json
    api("commons-io:commons-io:2.17.0") // Commons io
    api("org.apache.commons:commons-text:1.12.0") // StringSubstitutor

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0") // coroutine
    implementation("org.jline:jline:3.27.0") // CLI
    implementation("com.github.ajalt.clikt:clikt:5.0.1") // Run Arg
    implementation("org.fusesource.jansi:jansi:2.4.1") // AnsiConsole
    implementation("org.jsoup:jsoup:1.18.1") // Connection
    implementation(kotlin("reflect"))
}

tasks.named<ShadowJar>("shadowJar") {
    // [archiveBaseName]-[archiveAppendix]-[archiveVersion]-[archiveClassifier].[archiveExtension]

    archiveBaseName = rootProject.name
    archiveAppendix = "${properties["prefix"]}"
    archiveVersion = "$version"
    archiveClassifier = ""
    destinationDirectory = outputPath

    manifest {
        attributes("Main-Class" to "tw.xserver.loader.MainKt")
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

kotlin {
    jvmToolchain(21)
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly(project(":"))
    }

    kotlin {
        jvmToolchain(21)
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget = JvmTarget.JVM_21
    }

    tasks.build {
        dependsOn(tasks.jar)
    }
}
