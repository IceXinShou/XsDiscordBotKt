val pluginName = "IntervalPusher"
group = "tw.xserver.plugin"
version = "v1.0"

plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
}

dependencies {
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

tasks.named<Jar>("jar") {
    val outputPath: File by rootProject.extra

    archiveBaseName = pluginName
    archiveAppendix = "${properties["prefix"]}"
    archiveVersion = "$version"
    archiveClassifier = ""
    archiveExtension = "jar"
    destinationDirectory = outputPath.resolve("plugins")
}