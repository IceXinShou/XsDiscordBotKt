rootProject.name = "XsDiscordBotKt"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include(":Plugins:API:GoogleSheetAPI")
findProject(":Plugins:API:GoogleSheetAPI")?.name = "GoogleSheetAPI"

include(":Plugins:Economy")
findProject(":Plugins:Economy")?.name = "Economy"

include("Plugins:MessageCreator")
findProject(":Plugins:MessageCreator")?.name = "MessageCreator"

include("Plugins:Placeholder")
findProject(":Plugins:Placeholder")?.name = "Placeholder"
