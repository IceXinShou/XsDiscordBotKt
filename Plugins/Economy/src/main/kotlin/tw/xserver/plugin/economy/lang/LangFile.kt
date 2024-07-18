package tw.xserver.plugin.economy.lang

import kotlinx.serialization.Serializable

@Serializable
internal data class LangFile(
    val balance: Command1,
    val top_money: SimpleCommand,
    val top_cost: SimpleCommand,
    val add_money: Command2,
    val remove_money: Command2,
    val set_money: Command2,
    val set_cost: Command2
) {
    @Serializable
    internal data class Command1(
        val name: String,
        val description: String,
        val options: Options1
    )

    @Serializable
    internal data class Command2(
        val name: String,
        val description: String,
        val options: Options2
    )

    @Serializable
    internal data class Options1(
        val member: Option,
    )

    @Serializable
    internal data class Options2(
        val member: Option,
        val value: Option
    )

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
