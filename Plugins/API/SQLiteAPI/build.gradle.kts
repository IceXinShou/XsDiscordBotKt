import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val pluginName = "SQLiteAPI"
group = "tw.xserver.api"
version = "v2.0"

plugins {
    id("com.gradleup.shadow") version "8.3.5"
}

dependencies {
    api("org.xerial:sqlite-jdbc:3.47.1.0")
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
