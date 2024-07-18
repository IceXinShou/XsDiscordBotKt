package tw.xserver.loader.cli

import asg.cliche.Command
import asg.cliche.Param
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import tw.xserver.loader.error.Exceptions

/**
 * This class provides CLI functionality for interacting with a specific guild channel.
 * It allows sending messages directly from the command line to a channel within a Discord guild.
 */
class GuildChannelCLI(guild: Guild, private val channel: GuildChannel) : GuildCLI(guild) {

    /**
     * Sends a text message to the specified channel if it is a type of MessageChannel.
     * Throws an exception if the channel is not capable of handling messages.
     *
     * @param content The content of the message to be sent.
     * @throws Exceptions if the channel type is unknown or not a message channel.
     */
    @Command(name = "say", abbrev = "say", description = "Send a message to the channel of the guild.")
    @Throws(Exception::class)
    fun say(
        @Param(name = "content", description = "The content of the message to be sent.")
        content: String
    ) {
        if (channel is MessageChannel) {
            channel.sendMessage(content).queue()
        } else {
            throw Exceptions("The channel does not support sending text messages.")
        }
    }
}
