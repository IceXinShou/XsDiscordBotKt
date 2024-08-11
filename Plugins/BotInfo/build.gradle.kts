val pluginName = "BotInfo"
group = "tw.xserver.plugin"
version = "v2.0"

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20-RC"
}


dependencies {
    compileOnly(project(":Plugins:MessageCreator"))
    compileOnly(project(":Plugins:Placeholder"))
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
