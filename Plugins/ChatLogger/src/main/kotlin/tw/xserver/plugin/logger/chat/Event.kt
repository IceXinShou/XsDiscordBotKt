package tw.xserver.plugin.logger.chat


import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.localizations.LangManager
import tw.xserver.loader.plugin.PluginEvent
import tw.xserver.loader.util.FileGetter
import tw.xserver.loader.util.GlobalUtil
import tw.xserver.plugin.api.sqlite.SQLiteFileManager
import tw.xserver.plugin.logger.chat.command.getGuildCommands
import tw.xserver.plugin.logger.chat.lang.CmdFileSerializer
import tw.xserver.plugin.logger.chat.lang.CmdLocalizations
import java.io.File


/**
 * Main class for the Economy plugin managing configurations, commands, and data handling.
 */
object Event : PluginEvent(true) {
    internal const val DIR_PATH = "./plugins/ChatLogger/"
    internal const val COMPONENT_PREFIX = "xs:chat-logger:v2:"
    private val dbManager: SQLiteFileManager = SQLiteFileManager()
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun load() {
        reloadAll()
    }

    override fun unload() {}

    override fun reloadConfigFile() {
        fileGetter = FileGetter(DIR_PATH, this.javaClass)

        val dataFolder = File(DIR_PATH, "data")
        if (dataFolder.mkdirs()) {
            logger.info("Default data folder created")
        }

        dataFolder.listFiles()?.forEach { file ->
            if (file.isFile && file.extension == "db")
                dbManager.addFileConnection(file.nameWithoutExtension.toLong(), file)
        }
        logger.info("Data file loaded successfully")
    }

    override fun reloadLang() {
        LangManager(
            fileGetter = fileGetter,
            defaultLocale = DiscordLocale.CHINESE_TAIWAN,
            clazzD = CmdFileSerializer::class,
            clazzL = CmdLocalizations::class
        )
    }

    override fun guildCommands(): Array<CommandData> = getGuildCommands()

    /**
     * Initializes data handling when the bot is ready.
     */
    override fun onReady(event: ReadyEvent) {
        JsonManager.init()
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (GlobalUtil.checkCommandName(event, "chat-logger setting")) return

        ChatLogger.setting(event)
    }

    override fun onEntitySelectInteraction(event: EntitySelectInteractionEvent) {
        if (GlobalUtil.checkCommandPrefix(event, COMPONENT_PREFIX)) return

        ChatLogger.select(event)
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (GlobalUtil.checkCommandPrefix(event, COMPONENT_PREFIX)) return

        when (event.componentId) {
            "toggle" -> ChatLogger.toggle(event)
            "modify-allow", "modify-block" -> ChatLogger.createSel(event)
            "delete" -> ChatLogger.delete(event)
        }
    }
}
