package tw.xserver.plugin.botinfo.lang

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CmdFileSerializer(
    @SerialName("bot_info")
    val botInfo: SimpleCommand
) {
    @Serializable
    internal data class SimpleCommand(
        val name: String,
        val description: String
    )
}
