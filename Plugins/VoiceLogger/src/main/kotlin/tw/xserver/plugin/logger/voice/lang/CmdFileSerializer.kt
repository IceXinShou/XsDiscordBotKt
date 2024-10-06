package tw.xserver.plugin.logger.voice.lang

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CmdFileSerializer(
    @SerialName("voice-logger")
    val voiceLogger: Command,
) {
    @Serializable
    internal data class Command(
        val name: String,
        val description: String,
        val subcommands: SubCommands,
    ) {
        @Serializable
        internal data class SubCommands(
            val setting: SimpleCommand
        )

        @Serializable
        internal data class SimpleCommand(
            val name: String,
            val description: String
        )
    }
}
