package tw.xserver.plugin.botinfo.cmd

import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import tw.xserver.plugin.botinfo.lang.Localizations

/**
 * Retrieves and constructs an array of guild-specific command configurations.
 * Each command is tailored for economic transactions like checking balance, modifying balances, and viewing leaderboards.
 *
 * @return Array<CommandData> Collection of guild commands configured with localizations and permissions.
 */


fun getGuildCommands(): Array<CommandData> = arrayOf(
    // Command to display bot info message
    Commands.slash("bot-info", "show about the bot data")
        .setNameLocalizations(Localizations.botInfo.name)
        .setDescriptionLocalizations(Localizations.botInfo.description)
        .setDefaultPermissions(DefaultMemberPermissions.ENABLED),
)
