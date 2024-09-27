package tw.xserver.loader.util

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import tw.xserver.loader.base.MainLoader

/**
 * A utility class for fetching and managing entities like Users and Members from JDA.
 */
object GlobalUtil {
    /**
     * Retrieves a User object from Discord's servers by their Id.
     *
     * @param id The unique Id of the user.
     * @return The User object.
     */
    fun getUserById(id: Long): CacheRestAction<User> = MainLoader.jdaBot.retrieveUserById(id)
    fun getUserById(id: String): CacheRestAction<User> = MainLoader.jdaBot.retrieveUserById(id)

    /**
     * Gets the nickname of a user in a guild or their username if the nickname is not available.
     *
     * @param user The user whose name is being retrieved.
     * @param guild The guild from which to retrieve the member's nickname.
     * @return The nickname or username of the user.
     */
    fun getNickOrName(user: User, guild: Guild): String {
        val member: Member? = guild.retrieveMemberById(user.idLong).complete()
        return member?.nickname?.let { "$it (${user.name})" } ?: user.name
    }

    /**
     * Checks if the command name of the given SlashCommandInteractionEvent matches the provided name.
     *
     * @param event The SlashCommandInteractionEvent to check.
     * @param fullName The name to compare with the event's command name.
     * @return True if the names do not match, false otherwise.
     */
    fun checkCommandName(event: SlashCommandInteractionEvent, fullName: String): Boolean =
        event.fullCommandName != fullName

    fun checkPrefix(event: EntitySelectInteractionEvent, prefix: String): Boolean =
        !event.componentId.startsWith(prefix)

    fun checkPrefix(event: StringSelectInteractionEvent, prefix: String): Boolean =
        !event.componentId.startsWith(prefix)

    fun checkPrefix(event: ButtonInteractionEvent, prefix: String): Boolean =
        !event.componentId.startsWith(prefix)
}
