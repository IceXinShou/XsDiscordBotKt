package tw.xserver.plugin.botinfo

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.localizations.LangManager
import tw.xserver.loader.plugin.PluginEvent
import tw.xserver.loader.util.FileGetter
import tw.xserver.loader.util.GlobalUtil.checkCommandName
import tw.xserver.plugin.botinfo.cmd.getGuildCommands
import tw.xserver.plugin.botinfo.lang.LangFileSerializer
import tw.xserver.plugin.botinfo.lang.Localizations
import tw.xserver.plugin.creator.message.CreatorImpl
import tw.xserver.plugin.placeholder.PAPI

object BotInfo : PluginEvent(true) {
    private val logger: Logger = LoggerFactory.getLogger(BotInfo::class.java)
    private const val DIR_PATH = "./plugins/BotInfo/"
    private val creator = CreatorImpl("$DIR_PATH./lang/")

    override fun load() {
        reloadAll()
    }

    override fun unload() {}

    override fun reloadConfigFile() {
        getter = FileGetter(DIR_PATH, BotInfo::class.java)
    }

    override fun reloadLang() {
        LangManager(
            getter,
            DiscordLocale.CHINESE_TAIWAN,
            LangFileSerializer::class,
            Localizations::class
        )
    }

    override fun globalCommands(): Array<CommandData> = getGuildCommands()

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (checkCommandName(event, "bot-info")) return
        val locale = event.userLocale
        val memberCounts = event.jda.guilds.sumOf { guild ->
            guild.memberCount
        }

        PAPI.globalPlaceholder.put("member_counts", "$memberCounts")
        PAPI.globalPlaceholder.put("guild_counts", "${event.jda.guilds.size}")

        event.hook.editOriginal(
            creator.getBuilder(locale, "bot_info").build()
        ).queue()
    }
}