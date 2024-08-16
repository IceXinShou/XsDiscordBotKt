package tw.xserver.plugin.economy.lang

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CmdFileSerializer(
    val balance: Command1,

    @SerialName("top_money")
    val topMoney: SimpleCommand,

    @SerialName("top_cost")
    val topCost: SimpleCommand,

    @SerialName("add_money")
    val addMoney: Command2,

    @SerialName("remove_money")
    val removeMoney: Command2,

    @SerialName("set_money")
    val setMoney: Command2,

    @SerialName("set_cost")
    val setCost: Command2
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
    internal data class Command2(
        val name: String,
        val description: String,
        val options: Options2
    ) {
        @Serializable
        internal data class Options2(
            val member: Option,
            val value: Option
        )
    }

    @Serializable
    internal data class Option(
        val name: String,
        val description: String
    )

    @Serializable
    internal data class SimpleCommand(
        val name: String,
        val description: String
    )
}
