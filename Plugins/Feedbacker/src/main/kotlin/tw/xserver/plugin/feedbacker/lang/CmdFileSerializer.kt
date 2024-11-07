package tw.xserver.plugin.feedbacker.lang

import kotlinx.serialization.Serializable

@Serializable
internal data class CmdFileSerializer(
    val feedbacker: Command1,
) {
    @Serializable
    internal data class Command1(
        val name: String,
        val description: String,
        val options: Options1
    ) {
        @Serializable
        internal data class Options1(
            val member: Option
        )
    }

    @Serializable
    internal data class Option(
        val name: String,
        val description: String
    )
}
