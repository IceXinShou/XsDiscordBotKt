package tw.xserver.plugin.addons.ticket

import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import tw.xserver.plugin.addons.ticket.Event.PLUGIN_DIR_FILE
import tw.xserver.plugin.addons.ticket.Event.config
import tw.xserver.plugin.addons.ticket.Event.targetMember
import tw.xserver.plugin.creator.message.MessageCreator
import java.io.File

object TicketAddons {
    private val creator = MessageCreator(File(PLUGIN_DIR_FILE, "lang"), DiscordLocale.CHINESE_TAIWAN)

    fun onCreate(event: ChannelCreateEvent) {
        if (!event.isFromGuild && event.guild.id != config.guildId) return
        if (!event.channel.name.startsWith(config.prefix)) return
        if (targetMember.onlineStatus != OnlineStatus.OFFLINE) return

        event.channel.asTextChannel().sendMessage(
            creator.getCreateBuilder(
                "not_online",
                DiscordLocale.CHINESE_TAIWAN
            ).build()
        ).queue()
    }
}