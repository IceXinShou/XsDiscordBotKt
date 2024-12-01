package tw.xserver.loader.base

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.base.SettingsLoader.token
import tw.xserver.loader.builtin.statuschanger.StatusChanger
import tw.xserver.loader.logger.InteractionLogger
import tw.xserver.loader.util.Arguments
import kotlin.system.exitProcess

/**
 * Main loader for the bot application, handles bot initialization, and management of events and plugins.
 */
object BotLoader {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val ROOT_PATH: String = System.getProperty("user.dir")
    lateinit var jdaBot: JDA
        private set
    lateinit var bot: User
        private set

    /**
     * Starts the bot by loading settings, initializing plugins, and setting up JDA.
     */
    fun start() {
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

        jdaBot = JDABuilder.createDefault(Arguments.botToken ?: token)
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
            )
            .build()
            .awaitReady()

        bot = jdaBot.selfUser
        jdaBot.apply {
            addEventListener(ListenerManager(PluginLoader.guildCommands))
            addEventListener(InteractionLogger)
            PluginLoader.listenersQueue.forEach { plugin -> addEventListener(plugin) }
            updateCommands().addCommands(PluginLoader.globalCommands).queue()
        }

        logger.info("Bot initialized.")
    }

    /**
     * Reloads plugins and settings, resets status changer.
     */
    fun reload() {
        try {
            PluginLoader.reload()
            SettingsLoader.run()
            StatusChanger.run()
            logger.info("Application reloaded successfully.")
        } catch (e: Exception) {
            logger.error("Failed to reload application:", e)
        }
    }

    /**
     * Stops the bot and cleans up resources, including shutting down JDA and unloading plugins.
     */
    fun stop() {
        try {
            if (::jdaBot.isInitialized) {
                jdaBot.apply {
                    registeredListeners.forEach { removeEventListener(it) }
                    shutdown()
                    awaitShutdown()
                }
            }

            PluginLoader.apply {
                pluginQueue.reversed().forEach { (name, plugin) ->
                    plugin.unload()
                    logger.info("{} unloaded successfully.", name)
                }
            }

            StatusChanger.stop()
            logger.info("Bot shutdown completed.")
        } catch (e: Exception) {
            logger.error("Failed to stop BotLoader:", e)
        }
    }
}
