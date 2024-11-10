package tw.xserver.plugin.economy

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.json.JsonObjFileManager
import tw.xserver.loader.localizations.LangManager
import tw.xserver.loader.plugin.PluginEvent
import tw.xserver.loader.util.FileGetter
import tw.xserver.loader.util.GlobalUtil
import tw.xserver.plugin.economy.command.getGuildCommands
import tw.xserver.plugin.economy.lang.CmdFileSerializer
import tw.xserver.plugin.economy.lang.Localizations
import tw.xserver.plugin.economy.serializer.MainConfigSerializer
import tw.xserver.plugin.economy.storage.JsonImpl
import tw.xserver.plugin.economy.storage.SheetImpl
import tw.xserver.plugin.economy.storage.StorageInterface
import java.io.File
import java.io.IOException

/**
 * Main class for the Economy plugin managing configurations, commands, and data handling.
 */
object Event : PluginEvent(true) {
    internal val PLUGIN_DIR_FILE = File("./plugins/Economy/")
    internal const val COMPONENT_PREFIX = "xs:economy:v2:"
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    private val MODE = Mode.Json
    internal lateinit var config: MainConfigSerializer
    internal lateinit var storageManager: StorageInterface


    override fun load() {
        reloadAll()
    }

    override fun unload() {}

    override fun reloadConfigFile() {
        fileGetter = FileGetter(PLUGIN_DIR_FILE, this.javaClass)

        try {
            fileGetter.readInputStream("./config.yml").use {
                config = Yaml().decodeFromStream<MainConfigSerializer>(it)
            }
        } catch (e: IOException) {
            logger.error("Please configure {}./config.yml.", PLUGIN_DIR_FILE.canonicalPath, e)
        }

        logger.info("Setting file loaded successfully.")
        if (File(PLUGIN_DIR_FILE, "data").mkdirs()) {
            logger.info("Default data folder created.")
        }

        when (MODE) {
            Mode.Json -> {
                JsonImpl.json = JsonObjFileManager(File(PLUGIN_DIR_FILE, "data/data.json"))
                storageManager = JsonImpl
            }

            Mode.GoogleSheet -> {
                storageManager = SheetImpl
            }
        }

        logger.info("Data file loaded successfully.")
    }

    override fun reloadLang() {
        fileGetter.exportDefaultDirectory("./lang")

        LangManager(
            pluginDirFile = PLUGIN_DIR_FILE,
            fileName = "register.yml",
            defaultLocale = DiscordLocale.CHINESE_TAIWAN,
            clazzSerializer = CmdFileSerializer::class,
            clazzLocalization = Localizations::class
        )
    }

    override fun guildCommands(): Array<CommandData> = getGuildCommands()

    /**
     * Initializes data handling when the bot is ready.
     */
    override fun onReady(event: ReadyEvent) {
        storageManager.init()
        storageManager.sortMoneyBoard()
        storageManager.sortCostBoard()
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name.startsWith("top-")) {
            Economy.handleTopCommands(event)
            return
        }

        when (event.name) {
            "balance" -> Economy.handleBalance(event)
            "add-money", "remove-money", "set-money", "add-cost", "remove-cost", "set-cost" ->
                Economy.handleMoneyAndCostCommands(event)
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (GlobalUtil.checkPrefix(event, COMPONENT_PREFIX)) return
        ButtonReplier.onQuery(event)
    }

    private enum class Mode {
        Json,
        GoogleSheet,
    }
}
