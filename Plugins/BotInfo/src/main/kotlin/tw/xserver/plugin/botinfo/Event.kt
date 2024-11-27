package tw.xserver.plugin.botinfo

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import tw.xserver.loader.localizations.LangManager
import tw.xserver.loader.plugin.PluginEvent
import tw.xserver.loader.util.FileGetter
import tw.xserver.loader.util.GlobalUtil
import tw.xserver.plugin.botinfo.command.getGuildCommands
import tw.xserver.plugin.botinfo.lang.CmdFileSerializer
import tw.xserver.plugin.botinfo.lang.CmdLocalizations
import java.io.File

object Event : PluginEvent(true) {
    internal val PLUGIN_DIR_FILE = File("./plugins/BotInfo/")

    override fun load() {
        reloadAll()
    }

    override fun unload() {}

    override fun reloadConfigFile() {
        fileGetter = FileGetter(PLUGIN_DIR_FILE, this.javaClass)
    }

    override fun reloadLang() {
        fileGetter.exportDefaultDirectory("./lang")

        LangManager(
            pluginDirFile = PLUGIN_DIR_FILE,
            fileName = "register.yml",
            defaultLocale = DiscordLocale.CHINESE_TAIWAN,
            clazzSerializer = CmdFileSerializer::class,
            clazzLocalization = CmdLocalizations::class
        )
    }

    override fun globalCommands(): Array<CommandData> = getGuildCommands()

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (GlobalUtil.checkCommandName(event, "bot-info")) return

        BotInfo.reply(event)
    }
}