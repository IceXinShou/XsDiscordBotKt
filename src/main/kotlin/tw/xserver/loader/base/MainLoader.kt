package tw.xserver.loader.base

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.builtin.statuschanger.StatusChanger
import tw.xserver.loader.logger.InteractionLogger
import tw.xserver.loader.plugin.PluginEvent
import tw.xserver.loader.util.Arguments
import java.util.*
import kotlin.system.exitProcess

/**
 * Main loader for the bot application, handles bot initialization, and management of events and plugins.
 */
object MainLoader {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    val ROOT_PATH: String = System.getProperty("user.dir")
    val guildCommands: ArrayList<CommandData> = ArrayList()
    val globalCommands: ArrayList<CommandData> = ArrayList()
    val listenersQueue: Queue<PluginEvent> = ArrayDeque()
    lateinit var jdaBot: JDA
    lateinit var bot: User

    /**
     * Starts the bot by loading settings, initializing plugins, and setting up JDA.
     */
    fun main() {
        if (UpdateChecker.versionCheck()) {
            logger.error("Version check failed, exiting.")
            exitProcess(2)
        }

        SettingsLoader.run()
        PluginLoader.run()

        if (Arguments.noBuild) {
            logger.warn("Skip building bot!")
            return
        }

        jdaBot = JDABuilder.createDefault(SettingsLoader.token)
            .setBulkDeleteSplittingEnabled(false)
            .setLargeThreshold(250)
            .setMemberCachePolicy(MemberCachePolicy.DEFAULT)
            .enableCache(
                CacheFlag.VOICE_STATE,
            )
            .enableIntents(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
            ).build().apply {
                selfUser.also { bot = it }

                addEventListener(ListenerManager(guildCommands))
                addEventListener(InteractionLogger)
                listenersQueue.forEach { plugin -> addEventListener(plugin) }
                updateCommands().addCommands(globalCommands).queue()
            }

        logger.info("Bot initialized.")
    }

    /**
     * Reloads plugins and settings, resets status changer.
     */
    fun reload() {
        PluginLoader.reload()
        SettingsLoader.run()
        StatusChanger.run()
    }

    /**
     * Stops the bot and cleans up resources, including shutting down JDA and unloading plugins.
     */
    fun stop() {
        if (::jdaBot.isInitialized) {
            jdaBot.apply {
                registeredListeners.forEach { removeEventListener(it) }
                shutdown()
            }
        }

        PluginLoader.apply {
            pluginQueue.reversed().forEach { (name, plugin) ->
                plugin.unload()
                logger.info("{} unload successfully", name)
            }
        }

        StatusChanger.stop()
        logger.info("Bot shutdown completed.")
    }
}
