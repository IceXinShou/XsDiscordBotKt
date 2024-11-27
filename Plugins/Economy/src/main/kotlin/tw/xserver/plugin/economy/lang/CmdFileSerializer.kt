package tw.xserver.plugin.economy.lang

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tw.xserver.loader.localizations.LocalTemplate

@Serializable
internal data class CmdFileSerializer(
    val balance: CommandMember,

    @SerialName("top-money")
    val topMoney: LocalTemplate.NDString,

    @SerialName("top-cost")
    val topCost: LocalTemplate.NDString,

    @SerialName("add-money")
    val addMoney: CommandMemberValue,

    @SerialName("remove-money")
    val removeMoney: CommandMemberValue,

    @SerialName("set-money")
    val setMoney: CommandMemberValue,

    @SerialName("set-cost")
    val setCost: CommandMemberValue
) {

    @Serializable
    internal data class CommandMember(
        val name: String,
        val description: String,
        val options: Options
    ) {
        @Serializable
        internal data class Options(
            val member: LocalTemplate.NDString
        )
    }

    @Serializable
    internal data class CommandMemberValue(
        val name: String,
        val description: String,
        val options: Options
    ) {
        @Serializable
        internal data class Options(
            val member: LocalTemplate.NDString,
            val value: LocalTemplate.NDString
        )
    }
}
