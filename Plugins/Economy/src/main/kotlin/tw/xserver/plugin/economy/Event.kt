package tw.xserver.plugin.economy

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.user.update.UserUpdateGlobalNameEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.localizations.LangManager
import tw.xserver.loader.plugin.PluginEvent
import tw.xserver.loader.util.FileGetter
import tw.xserver.loader.util.json.JsonObjFileManager
import tw.xserver.plugin.economy.command.getGuildCommands
import tw.xserver.plugin.economy.lang.CmdFileSerializer
import tw.xserver.plugin.economy.lang.Localizations
import tw.xserver.plugin.economy.serializer.MainConfigSerializer
import tw.xserver.plugin.economy.storage.JsonManager
import java.io.File
import java.io.IOException

/**
 * Main class for the Economy plugin managing configurations, commands, and data handling.
 */
object Event : PluginEvent(true) {
    internal const val DIR_PATH = "./plugins/Economy/"
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    internal val MODE = Economy.Mode.Json
    internal lateinit var config: MainConfigSerializer

    override fun load() {
        reloadAll()
    }

    override fun unload() {}

    override fun reloadConfigFile() {
        fileGetter = FileGetter(DIR_PATH, this.javaClass)

        try {
            fileGetter.readInputStream("./config.yml").use {
                config = Yaml().decodeFromStream<MainConfigSerializer>(it)
            }
        } catch (e: IOException) {
            logger.error("Please configure ${DIR_PATH}./config.yml", e)
        }

        logger.info("Setting file loaded successfully")
        if (File(DIR_PATH, "data").mkdirs()) {
            logger.info("Default data folder created")
        }

        if (MODE == Economy.Mode.Json)
            JsonManager.json = JsonObjFileManager(File(DIR_PATH, "data/data.json"))
        logger.info("Data file loaded successfully")
    }

    override fun reloadLang() {
        LangManager(
            fileGetter = fileGetter,
            defaultLocale = DiscordLocale.ENGLISH_US,
            clazzD = CmdFileSerializer::class,
            clazzL = Localizations::class
        )
    }

    override fun guildCommands(): Array<CommandData> = getGuildCommands()

    /**
     * Initializes data handling when the bot is ready.
     */
    override fun onReady(event: ReadyEvent) {
        if (MODE == Economy.Mode.Json)
            JsonManager.initFile()
        Economy.updateMoneyBoard()
        Economy.updateCostBoard()
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name.startsWith("top-")) {
            Economy.handleTopCommands(event)
            return
        }

        when (event.name) {
            "balance" -> Economy.handleBalance(event)
            "add-money", "remove-money", "set-money", "set-cost" -> Economy.handleMoneyAndCostCommands(event)
        }
    }

    /**
     * Updates user's global name changes in the data storage.
     */
    override fun onUserUpdateGlobalName(event: UserUpdateGlobalNameEvent) =
        JsonManager.nameUpdate(event.user)

}
