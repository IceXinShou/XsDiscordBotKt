package tw.xserver.plugin.botinfo

import tw.xserver.plugin.botinfo.cmd.getGuildCommands
import tw.xserver.plugin.botinfo.lang.Localizations
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.localizations.LangManager
import tw.xserver.loader.plugin.PluginEvent
import tw.xserver.loader.util.FileGetter
import tw.xserver.loader.util.GlobalUtil.checkCommandName
import tw.xserver.plugin.botinfo.lang.LangFileSerializer
import tw.xserver.plugin.creator.message.MessageCreator
import java.time.OffsetDateTime

object BotInfo : PluginEvent(true) {
    private val logger: Logger = LoggerFactory.getLogger(BotInfo::class.java)
    private const val DIR_PATH = "./plugins/BotInfo/"

    override fun load() {
        reloadAll()
        logger.info("loaded BotInfo")
    }

    override fun unload() {
        logger.info("unLoaded BotInfo")
    }

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

        val members = event.jda.guilds.sumOf { guild ->
            guild.memberCount
        }

//        MessageCreator.
//
//        val builder = EmbedBuilder()
//        builder.addField(
//            lang.runtime.successes.guild_count.get(locale),
//            event.jda.guilds.size.toLong().toString(), false
//        )
//        builder.addField(
//            lang.runtime.successes.member_count.get(locale),
//            members.toString(), false
//        )
//
//        event.hook.editOriginalEmbeds(
//            builder
//                .setTitle(lang.runtime.successes.title.get(locale))
//                .setTimestamp(OffsetDateTime.now())
//                .setColor(0x00FFFF)
//                .build()
//        ).queue()
    }
}