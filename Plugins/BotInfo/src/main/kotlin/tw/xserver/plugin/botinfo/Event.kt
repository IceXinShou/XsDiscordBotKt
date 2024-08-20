package tw.xserver.plugin.botinfo

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.localizations.LangManager
import tw.xserver.loader.plugin.PluginEvent
import tw.xserver.loader.util.FileGetter
import tw.xserver.loader.util.GlobalUtil
import tw.xserver.plugin.botinfo.command.getGuildCommands
import tw.xserver.plugin.botinfo.lang.CmdFileSerializer
import tw.xserver.plugin.botinfo.lang.Localizations
import tw.xserver.plugin.creator.message.MessageCreator
import tw.xserver.plugin.placeholder.Placeholder

object Event : PluginEvent(true) {
    private const val DIR_PATH = "./plugins/BotInfo/"
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    private val creator = MessageCreator("$DIR_PATH./lang/")

    override fun load() {
        reloadAll()
    }

    override fun unload() {}

    override fun reloadConfigFile() {
        fileGetter = FileGetter(DIR_PATH, this.javaClass)
    }

    override fun reloadLang() {
        LangManager(
            fileGetter = fileGetter,
            defaultLocale = DiscordLocale.ENGLISH_US,
            clazzD = CmdFileSerializer::class,
            clazzL = Localizations::class
        )
    }

    override fun globalCommands(): Array<CommandData> = getGuildCommands()

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (GlobalUtil.checkCommandName(event, "bot-info")) return
        val memberCounts = event.jda.guilds.sumOf { guild ->
            guild.memberCount
        }

        Placeholder.globalPlaceholder.put("member_counts", "$memberCounts")
        Placeholder.globalPlaceholder.put("guild_counts", "${event.jda.guilds.size}")

        event.hook.editOriginal(
            creator.getBuilder(event).build()
        ).queue()
    }
}