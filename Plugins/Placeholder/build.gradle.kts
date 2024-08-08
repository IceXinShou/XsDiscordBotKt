group = "tw.xserver.plugin"
version = "v2.0"

plugins {
    kotlin("jvm")
}


repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":"))
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("Plugins/Placeholder/src/main/kotlin"))
        }
    }
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("Placeholder-${properties["prefix"]}")
    archiveVersion.set("$version")
    archiveClassifier.set("")
    destinationDirectory.set(file("../../Server/plugins"))

    dependencies {
    }
}

kotlin {
    jvmToolchain(21)
}