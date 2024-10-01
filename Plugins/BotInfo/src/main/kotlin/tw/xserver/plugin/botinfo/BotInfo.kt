package tw.xserver.plugin.botinfo

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.utils.messages.MessageEditData
import tw.xserver.loader.builtin.placeholder.Placeholder
import tw.xserver.plugin.botinfo.Event.PLUGIN_DIR_FILE
import tw.xserver.plugin.creator.message.MessageCreator
import java.io.File

object BotInfo {
    private val creator = MessageCreator(File(PLUGIN_DIR_FILE, "lang"), DiscordLocale.CHINESE_TAIWAN)

    fun reply(event: SlashCommandInteractionEvent) {
        val memberCounts = event.jda.guilds.sumOf { guild ->
            guild.memberCount
        }

        Placeholder.globalPlaceholder.put("member_counts", "$memberCounts")
        Placeholder.globalPlaceholder.put("guild_counts", "${event.jda.guilds.size}")

        event.hook.editOriginal(
            MessageEditData.fromCreateData(
                creator.getCreateBuilder(event).build()
            )
        ).queue()
    }
}