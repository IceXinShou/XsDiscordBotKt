package tw.xserver.plugin.logger.chat.command

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import tw.xserver.plugin.logger.chat.lang.CmdLocalizations

fun getGuildCommands(): Array<CommandData> = arrayOf(
    Commands.slash("chat-logger", "commands about chat logger")
        .setNameLocalizations(CmdLocalizations.chatLogger.name)
        .setDescriptionLocalizations(CmdLocalizations.chatLogger.description)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        .addSubcommands(
            SubcommandData("setting", "set chat log in this channel")
                .setNameLocalizations(CmdLocalizations.chatLogger.subcommands.setting.name)
                .setDescriptionLocalizations(CmdLocalizations.chatLogger.subcommands.setting.description)
        )
)