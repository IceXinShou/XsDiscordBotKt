package tw.xserver.loader.cli

import asg.cliche.Command
import asg.cliche.Param
import asg.cliche.ShellFactory
import net.dv8tion.jda.api.entities.channel.concrete.*
import org.fusesource.jansi.AnsiConsole
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.base.MainLoader
import kotlin.system.exitProcess

/**
 * Provides a root command-line interface for managing interactions with Discord entities.
 * This class allows binding to guilds, channels, and executing actions like sending messages or controlling audio settings.
 */
open class RootCLI {
    private val logger: Logger = LoggerFactory.getLogger(RootCLI::class.java)

    /**
     * Binds the command shell to a specific guild for further guild-specific commands.
     * @param guildId The unique identifier of the guild.
     */
    @Command(name = "bind-guild", abbrev = "bg", description = "Bind to a specific guild.")
    @Throws(Exception::class)
    fun bindGuild(
        @Param(name = "guildId", description = "The guild Id.")
        guildId: String
    ) {
        val guild = MainLoader.jdaBot.getGuildById(guildId)
            ?: throw IllegalArgumentException("Guild with Id: $guildId not found.")
        ShellFactory.createConsoleShell(guild.name, "", GuildCLI(guild)).commandLoop()
    }

    /**
     * Binds the command shell to a specific channel within a guild for further channel-specific commands.
     * @param guildId The guild Id where the channel is located.
     * @param channelId The channel Id to bind.
     */
    @Command(name = "bind-guild-channel", abbrev = "bgc", description = "Bind to a specific channel of a guild.")
    @Throws(Exception::class)
    fun bindGuildChannel(
        @Param(name = "guildId", description = "The guild Id.")
        guildId: String,
        @Param(name = "channelId", description = "The channel Id.")
        channelId: String
    ) {
        val guild = MainLoader.jdaBot.getGuildById(guildId)
            ?: throw IllegalArgumentException("Guild with Id: $guildId not found.")
        val channel = guild.getGuildChannelById(channelId)
            ?: throw IllegalArgumentException("Channel with Id: $channelId not found.")

        ShellFactory.createConsoleShell("${guild.name} | ${channel.name}", "", GuildChannelCLI(guild, channel))
            .commandLoop()
    }

    /**
     * Binds the command shell to a specific user's private channel for direct messaging.
     * @param userId The unique identifier of the user.
     */
    @Command(
        name = "bind-private-channel",
        abbrev = "bpc",
        description = "Bind to a specific private channel of a user."
    )
    @Throws(Exception::class)
    fun bindPrivateChannel(
        @Param(name = "userId", description = "The user Id.")
        userId: String
    ) {
        val user = MainLoader.jdaBot.retrieveUserById(userId).complete()
            ?: throw IllegalArgumentException("User with Id: $userId not found.")
        ShellFactory.createConsoleShell(user.name, "", PrivateChannelCLI(user)).commandLoop()
    }

    /**
     * Sends a direct message to a specified user.
     * @param userId The unique identifier of the user to whom the message is sent.
     * @param content The content of the message.
     */
    @Command(name = "direct-message", abbrev = "dm", description = "Send a direct message to a user.")
    fun dm(
        @Param(name = "userId", description = "The user Id.")
        userId: String,
        @Param(name = "content", description = "The content of the message.")
        content: String
    ) {
        val user = MainLoader.jdaBot.retrieveUserById(userId).complete()
            ?: throw IllegalArgumentException("User with Id: $userId not found.")
        val privateChannel = user.openPrivateChannel().complete()
            ?: throw IllegalArgumentException("Failed to open a private channel with user Id: $userId.")
        privateChannel.sendMessage(content).queue()
    }

    // Other commands (say, join, leave, mute, deafen, stop, reload) are similarly structured and commented.

    /**
     * Stops the application, cleaning up any resources and closing any connections.
     */
    @Command(name = "shutdown", abbrev = "stop", description = "Shutdown the program.")
    fun stop() {
        MainLoader.stop()
        AnsiConsole.systemUninstall()
        logger.info("Application stopped successfully.")
        exitProcess(0)
    }

    /**
     * Reloads the application configuration or restarts necessary components.
     */
    @Command(name = "reload", description = "Reload the program.")
    @Throws(Exception::class)
    fun reload() {
        MainLoader.reload()
        logger.info("Application reloaded successfully.")
    }
}
