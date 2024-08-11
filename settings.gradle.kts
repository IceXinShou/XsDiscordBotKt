rootProject.name = "XsDiscordBotKt"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include(":Plugins:API:GoogleSheetAPI")
findProject(":Plugins:API:GoogleSheetAPI")?.name = "GoogleSheetAPI"

include(":Plugins:Economy")
findProject(":Plugins:Economy")?.name = "Economy"

include("Plugins:MessageCreator")
findProject(":Plugins:MessageCreator")?.name = "MessageCreator"

include("Plugins:Placeholder")
findProject(":Plugins:Placeholder")?.name = "Placeholder"

include("Plugins:BotInfo")
findProject(":Plugins:BotInfo")?.name = "BotInfo"
