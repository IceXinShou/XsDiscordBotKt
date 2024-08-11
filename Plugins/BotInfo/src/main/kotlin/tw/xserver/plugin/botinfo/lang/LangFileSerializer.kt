package tw.xserver.plugin.botinfo.lang

import kotlinx.serialization.Serializable

@Serializable
internal data class LangFileSerializer(
    val bot_info: SimpleCommand
) {
    @Serializable
    internal data class SimpleCommand(
        val name: String,
        val description: String
    )
}
