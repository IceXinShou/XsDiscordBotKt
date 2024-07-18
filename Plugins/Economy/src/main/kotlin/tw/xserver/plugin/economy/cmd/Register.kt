package tw.xserver.plugin.economy.cmd

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
        .setNameLocalizations(Localizations.top_money.name)
        .setDescriptionLocalizations(Localizations.top_money.description)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

    // Command to display top transaction logs.
    Commands.slash("top-cost", "Get leaderboard from log money")
        .setNameLocalizations(Localizations.top_cost.name)
        .setDescriptionLocalizations(Localizations.top_cost.description)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

    // Command to add money to a member's balance.
    Commands.slash("add-money", "Add money to member's balance")
        .setNameLocalizations(Localizations.add_money.name)
        .setDescriptionLocalizations(Localizations.add_money.description)
        .addOptions(
            OptionData(OptionType.USER, "member", "Specify the member to modify.", true)
                .setNameLocalizations(Localizations.add_money.options.member.name)
                .setDescriptionLocalizations(Localizations.add_money.options.member.description),
            OptionData(OptionType.INTEGER, "value", "Specify the amount to add.", true)
                .setNameLocalizations(Localizations.add_money.options.value.name)
                .setDescriptionLocalizations(Localizations.add_money.options.value.description)
        )
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

    // Command to remove money from a member's balance.
    Commands.slash("remove-money", "Remove money from member's balance")
        .setNameLocalizations(Localizations.remove_money.name)
        .setDescriptionLocalizations(Localizations.remove_money.description)
        .addOptions(
            OptionData(OptionType.USER, "member", "Specify the member to modify.", true)
                .setNameLocalizations(Localizations.remove_money.options.member.name)
                .setDescriptionLocalizations(Localizations.remove_money.options.member.description),
            OptionData(OptionType.INTEGER, "value", "Specify the amount to remove.", true)
                .setNameLocalizations(Localizations.remove_money.options.value.name)
                .setDescriptionLocalizations(Localizations.remove_money.options.value.description)
        )
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

    // Command to set a specific money value to a member's balance.
    Commands.slash("set-money", "Set money to member's balance")
        .setNameLocalizations(Localizations.set_money.name)
        .setDescriptionLocalizations(Localizations.set_money.description)
        .addOptions(
            OptionData(OptionType.USER, "member", "Specify the member to modify.", true)
                .setNameLocalizations(Localizations.set_money.options.member.name)
                .setDescriptionLocalizations(Localizations.set_money.options.member.description),
            OptionData(OptionType.INTEGER, "value", "Specify the new balance.", true)
                .setNameLocalizations(Localizations.set_money.options.value.name)
                .setDescriptionLocalizations(Localizations.set_money.options.value.description)
        )
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

    // Command to record a transaction log adjustment for a member.
    Commands.slash("set-cost", "Set money log to member")
        .setNameLocalizations(Localizations.set_cost.name)
        .setDescriptionLocalizations(Localizations.set_cost.description)
        .addOptions(
            OptionData(OptionType.USER, "member", "Specify the member to modify.", true)
                .setNameLocalizations(Localizations.set_cost.options.member.name)
                .setDescriptionLocalizations(Localizations.set_cost.options.member.description),
            OptionData(OptionType.INTEGER, "value", "Specify the log amount.", true)
                .setNameLocalizations(Localizations.set_cost.options.value.name)
                .setDescriptionLocalizations(Localizations.set_cost.options.value.description)
        )
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
)
