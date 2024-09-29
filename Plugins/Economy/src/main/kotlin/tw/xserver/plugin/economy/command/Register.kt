package tw.xserver.plugin.economy.command

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import tw.xserver.plugin.economy.lang.Localizations

/**
 * Retrieves and constructs an array of guild-specific command configurations.
 * Each command is tailored for economic transactions like checking balance, modifying balances, and viewing leaderboards.
 *
 * @return Array<CommandData> Collection of guild commands configured with localizations and permissions.
 */
fun getGuildCommands(): Array<CommandData> = arrayOf(
    // Command to retrieve current money balance of a member.
    Commands.slash("balance", "Get current money from member")
        .setNameLocalizations(Localizations.balance.name)
        .setDescriptionLocalizations(Localizations.balance.description)
        .addOptions(
            OptionData(OptionType.USER, "member", "Specify the member to query.")
                .setNameLocalizations(Localizations.balance.options.member.name)
                .setDescriptionLocalizations(Localizations.balance.options.member.description)
        )
        .setDefaultPermissions(DefaultMemberPermissions.ENABLED),

    // Command to display top money holders.
    Commands.slash("top-money", "Get leaderboard for money")
        .setNameLocalizations(Localizations.topMoney.name)
        .setDescriptionLocalizations(Localizations.topMoney.description)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

    // Command to display top transaction logs.
    Commands.slash("top-cost", "Get leaderboard from log money")
        .setNameLocalizations(Localizations.topCost.name)
        .setDescriptionLocalizations(Localizations.topCost.description)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

    // Command to add money to a member's balance.
    Commands.slash("add-money", "Add money to member's balance")
        .setNameLocalizations(Localizations.addMoney.name)
        .setDescriptionLocalizations(Localizations.addMoney.description)
        .addOptions(
            OptionData(OptionType.USER, "member", "Specify the member to modify.", true)
                .setNameLocalizations(Localizations.addMoney.options.member.name)
                .setDescriptionLocalizations(Localizations.addMoney.options.member.description),
            OptionData(OptionType.INTEGER, "value", "Specify the amount to add.", true)
                .setNameLocalizations(Localizations.addMoney.options.value.name)
                .setDescriptionLocalizations(Localizations.addMoney.options.value.description)
        )
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

    // Command to remove money from a member's balance.
    Commands.slash("remove-money", "Remove money from member's balance")
        .setNameLocalizations(Localizations.removeMoney.name)
        .setDescriptionLocalizations(Localizations.removeMoney.description)
        .addOptions(
            OptionData(OptionType.USER, "member", "Specify the member to modify.", true)
                .setNameLocalizations(Localizations.removeMoney.options.member.name)
                .setDescriptionLocalizations(Localizations.removeMoney.options.member.description),
            OptionData(OptionType.INTEGER, "value", "Specify the amount to remove.", true)
                .setNameLocalizations(Localizations.removeMoney.options.value.name)
                .setDescriptionLocalizations(Localizations.removeMoney.options.value.description)
        )
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

    // Command to set a specific money value to a member's balance.
    Commands.slash("set-money", "Set money to member's balance")
        .setNameLocalizations(Localizations.setMoney.name)
        .setDescriptionLocalizations(Localizations.setMoney.description)
        .addOptions(
            OptionData(OptionType.USER, "member", "Specify the member to modify.", true)
                .setNameLocalizations(Localizations.setMoney.options.member.name)
                .setDescriptionLocalizations(Localizations.setMoney.options.member.description),
            OptionData(OptionType.INTEGER, "value", "Specify the new balance.", true)
                .setNameLocalizations(Localizations.setMoney.options.value.name)
                .setDescriptionLocalizations(Localizations.setMoney.options.value.description)
        )
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

    // Command to record a transaction log adjustment for a member.
    Commands.slash("set-cost", "Set money log to member")
        .setNameLocalizations(Localizations.setCost.name)
        .setDescriptionLocalizations(Localizations.setCost.description)
        .addOptions(
            OptionData(OptionType.USER, "member", "Specify the member to modify.", true)
                .setNameLocalizations(Localizations.setCost.options.member.name)
                .setDescriptionLocalizations(Localizations.setCost.options.member.description),
            OptionData(OptionType.INTEGER, "value", "Specify the new balance.", true)
                .setNameLocalizations(Localizations.setCost.options.value.name)
                .setDescriptionLocalizations(Localizations.setCost.options.value.description)
        )
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),


    Commands.slash("add-cost", "Add money log to member")
        .setNameLocalizations(Localizations.addCost.name)
        .setDescriptionLocalizations(Localizations.addCost.description)
        .addOptions(
            OptionData(OptionType.USER, "member", "Specify the member to modify.", true)
                .setNameLocalizations(Localizations.addCost.options.member.name)
                .setDescriptionLocalizations(Localizations.addCost.options.member.description),
            OptionData(OptionType.INTEGER, "value", "Specify the amount to add.", true)
                .setNameLocalizations(Localizations.addCost.options.value.name)
                .setDescriptionLocalizations(Localizations.addCost.options.value.description)
        )
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

    Commands.slash("remove-cost", "Remove money log to member")
        .setNameLocalizations(Localizations.removeCost.name)
        .setDescriptionLocalizations(Localizations.removeCost.description)
        .addOptions(
            OptionData(OptionType.USER, "member", "Specify the member to modify.", true)
                .setNameLocalizations(Localizations.removeCost.options.member.name)
                .setDescriptionLocalizations(Localizations.removeCost.options.member.description),
            OptionData(OptionType.INTEGER, "value", "Specify the amount to remove.", true)
                .setNameLocalizations(Localizations.removeCost.options.value.name)
                .setDescriptionLocalizations(Localizations.removeCost.options.value.description)
        )
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
)
