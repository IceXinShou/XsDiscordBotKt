package tw.xserver.plugin.feedbacker.lang

import kotlinx.serialization.Serializable
import tw.xserver.loader.localizations.LocalTemplate

@Serializable
internal data class CmdFileSerializer(
    val feedbacker: Command1,
) {
    @Serializable
    internal data class Command1(
        val name: String,
        val description: String,
        val options: Options
    ) {
        @Serializable
        internal data class Options(
            val member: LocalTemplate.NDString
        )
    }
}
