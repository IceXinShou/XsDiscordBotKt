package tw.xserver.plugin.botinfo

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import tw.xserver.plugin.botinfo.Event.PLUGIN_DIR_FILE
import tw.xserver.plugin.creator.message.MessageCreator
import tw.xserver.plugin.placeholder.Placeholder
import java.io.File

object BotInfo {
    private val creator = MessageCreator(File(PLUGIN_DIR_FILE, "lang"))

    fun reply(event: SlashCommandInteractionEvent) {
        val memberCounts = event.jda.guilds.sumOf { guild ->
            guild.memberCount
        }

        Placeholder.globalPlaceholder.put("member_counts", "$memberCounts")
        Placeholder.globalPlaceholder.put("guild_counts", "${event.jda.guilds.size}")

        event.hook.editOriginal(
            creator.getEditBuilder(event).build()
        ).queue()
    }
}