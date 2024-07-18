package tw.xserver.loader.cli

import asg.cliche.Command
import asg.cliche.Param
import asg.cliche.ShellFactory
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import tw.xserver.loader.error.Exceptions

/**
 * Provides command-line interface functionality for managing and interacting with Discord guild channels.
 * Extends RootCLI to inherit basic CLI functionalities.
 */
open class GuildCLI(private val guild: Guild) : RootCLI() {

    /**
     * Binds to a specific guild channel to enable direct command execution within that context.
     * @param channelId The unique identifier for the guild channel.
     * @throws IllegalArgumentException if the channel Id does not correspond to a valid channel.
     */
    @Command(name = "bindc", abbrev = "bc", description = "Bind to a specific channel of the guild.")
    @Throws(Exception::class)
    fun bindGuildChannel(
        @Param(name = "channelId", description = "Specific channel Id.")
        channelId: String
    ) {
        val channel = guild.getGuildChannelById(channelId)
            ?: throw IllegalArgumentException("Guild channel not found for Id: $channelId")

        ShellFactory
            .createConsoleShell(guild.name + " | " + channel.name, "", GuildChannelCLI(guild, channel))
            .commandLoop()
    }

    /**
     * Sends a message to a specified guild channel if it is a MessageChannel.
     * @param channelId The Id of the channel to send the message to.
     * @param content The content of the message to be sent.
     * @throws IllegalArgumentException if the channel does not exist or is not a MessageChannel.
     */
    @Command(name = "say", abbrev = "say", description = "Send a message to the channel of the guild.")
    @Throws(Exception::class)
    fun say(
        @Param(name = "channelId", description = "Your TextChannel Id.")
        channelId: String,
        @Param(name = "content", description = "Message content.")
        content: String
    ) {
        val channel = guild.getGuildChannelById(channelId)
            ?: throw IllegalArgumentException("Guild channel not found for Id: $channelId")

        if (channel is MessageChannel)
            channel.sendMessage(content).queue()
        else
            throw Exceptions("Channel identified by Id: $channelId is not capable of sending messages.")
    }

    /**
     * Connects the bot to an audio channel within the guild.
     * @param channelId The Id of the AudioChannel to join.
     * @throws IllegalArgumentException if the channel Id does not correspond to a valid AudioChannel.
     */
    @Command(name = "join", abbrev = "jo", description = "Make the bot join the AudioChannel of the guild.")
    fun join(
        @Param(name = "channelId", description = "Your AudioChannel Id.")
        channelId: String
    ) {
        val channel = guild.getChannelById(AudioChannel::class.java, channelId)
            ?: throw IllegalArgumentException("Audio channel not found for Id: $channelId")

        guild.audioManager.openAudioConnection(channel)
    }

    /**
     * Disconnects the bot from any connected audio channel within the guild.
     */
    @Command(name = "leave", abbrev = "le", description = "Make the bot leave the AudioChannel of the guild.")
    fun leave() {
        guild.audioManager.closeAudioConnection()
    }

    /**
     * Toggles the mute state of the bot in the guild's audio channel.
     */
    @Command(name = "mute", abbrev = "mu", description = "Toggle the bot's mute state.")
    fun mute() {
        val manager = guild.audioManager
        manager.isSelfMuted = !manager.isSelfMuted
    }

    /**
     * Toggles the deafened state of the bot in the guild's audio channel.
     */
    @Command(name = "deafen", abbrev = "de", description = "Toggle the bot's deafened state.")
    fun deafen() {
        val manager = guild.audioManager
        manager.isSelfDeafened = !manager.isSelfDeafened
    }

    /**
     * Deletes a specific message from a message channel within the guild.
     * @param channelId The Id of the channel containing the message.
     * @param messageId The Id of the message to be deleted.
     * @throws IllegalArgumentException if the channel is not found or is not a MessageChannel.
     * @throws Exceptions if the channel type does not support message deletion.
     */
    @Command(name = "delete", abbrev = "del", description = "Delete a message from a specific channel.")
    @Throws(Exception::class)
    fun delete(
        @Param(name = "channelId", description = "Your TextChannel Id.")
        channelId: String,
        @Param(name = "messageId", description = "The Id of the message to delete.")
        messageId: String
    ) {
        val channel = guild.getGuildChannelById(channelId)
            ?: throw IllegalArgumentException("Guild channel not found for Id: $channelId")

        if (channel is MessageChannel) {
            channel.retrieveMessageById(messageId).queue { it.delete().queue() }
        } else {
            throw Exceptions("Channel identified by Id: $channelId does not support message deletion.")
        }
    }
}
