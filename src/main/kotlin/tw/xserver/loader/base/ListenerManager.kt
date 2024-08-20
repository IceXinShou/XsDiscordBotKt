package tw.xserver.loader.base

import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This class manages the initialization of all listeners and the registration of guild-specific commands.
 */
class ListenerManager(
    private val guildCommands: List<CommandData>,
) : ListenerAdapter() {

    /**
     * Handles the GuildReadyEvent which triggers when a guild becomes available to the bot.
     * This method registers the commands that are specific to the guild.
     */
    override fun onGuildReady(event: GuildReadyEvent) {

        val guild = event.guild
        if (guildCommands.isNotEmpty()) {
            guild.updateCommands().addCommands(guildCommands).queue()
        }
        logger.info("Guild loaded: {} ({})", guild.name, guild.id)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
