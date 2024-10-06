package tw.xserver.plugin.logger.voice.command

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import tw.xserver.plugin.logger.voice.lang.CmdLocalizations

fun getGuildCommands(): Array<CommandData> = arrayOf(
    Commands.slash("voice-logger", "commands about voice logger")
        .setNameLocalizations(CmdLocalizations.voiceLogger.name)
        .setDescriptionLocalizations(CmdLocalizations.voiceLogger.description)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        .addSubcommands(
            SubcommandData("setting", "set voice log in this channel")
                .setNameLocalizations(CmdLocalizations.voiceLogger.subcommands.setting.name)
                .setDescriptionLocalizations(CmdLocalizations.voiceLogger.subcommands.setting.description)
        )
)
