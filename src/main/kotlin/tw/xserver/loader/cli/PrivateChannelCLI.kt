package tw.xserver.loader.cli

import asg.cliche.Command
import asg.cliche.Param
import net.dv8tion.jda.api.entities.User

/**
 * Provides a command-line interface to interact with a Discord user's private channel.
 * This class allows sending direct messages to a user.
 */
class PrivateChannelCLI(private val user: User) : RootCLI() {

    /**
     * Sends a direct message to the user.
     * The method opens a private channel with the user and sends a specified message.
     *
     * @param content The content of the message to be sent.
     * @throws IllegalArgumentException If the private channel cannot be opened.
     */
    @Command(name = "dm", description = "Send a direct message to the user.")
    fun dm(
        @Param(name = "content", description = "The content of the message.")
        content: String
    ) {
        // Open a private channel and handle possible null result properly
        val privateChannel = user.openPrivateChannel().complete()
            ?: throw IllegalArgumentException("Failed to open a private channel with the user.")

        privateChannel.sendMessage(content).queue({}, { exception ->
            // Log or handle the failure case, possibly returning error to the CLI.
            throw RuntimeException("Failed to send the message: ${exception.message}", exception)
        }
        )
    }
}
