package tw.xserver.plugin.addons.ticket

import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import tw.xserver.loader.base.MainLoader.jdaBot
import tw.xserver.plugin.addons.ticket.Event.PLUGIN_DIR_FILE
import tw.xserver.plugin.addons.ticket.Event.config
import tw.xserver.plugin.creator.message.MessageCreator
import java.io.File
import java.util.concurrent.TimeUnit

object TicketAddons {
    private val creator = MessageCreator(File(PLUGIN_DIR_FILE, "lang"), DiscordLocale.CHINESE_TAIWAN)

    fun onCreate(event: ChannelCreateEvent) {
        if (!event.isFromGuild && event.guild.id != config.guildId) return
        if (!event.channel.name.startsWith(config.prefix)) return
        if (jdaBot.getGuildById(config.guildId)!!
                .retrieveMemberById(config.userId).complete()
                .onlineStatus != OnlineStatus.OFFLINE
        ) return

        val delay: Long = config.delayMillis

        event.channel.asTextChannel().sendMessage(
            creator.getCreateBuilder(
                "not_online",
                DiscordLocale.CHINESE_TAIWAN
            ).build()
        ).queueAfter(delay, TimeUnit.MILLISECONDS)
    }
}