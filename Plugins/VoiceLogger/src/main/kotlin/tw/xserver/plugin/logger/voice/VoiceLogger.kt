package tw.xserver.plugin.logger.voice

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateVoiceStatusEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.restaction.CacheRestAction
import net.dv8tion.jda.api.utils.messages.MessageEditData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.builtin.placeholder.Placeholder
import tw.xserver.loader.builtin.placeholder.Substitutor
import tw.xserver.plugin.creator.message.MessageCreator
import tw.xserver.plugin.logger.voice.Event.COMPONENT_PREFIX
import tw.xserver.plugin.logger.voice.Event.DEFAULT_LOCALE
import tw.xserver.plugin.logger.voice.Event.PLUGIN_DIR_FILE
import tw.xserver.plugin.logger.voice.JsonManager.dataMap
import tw.xserver.plugin.logger.voice.lang.PlaceholderLocalizations
import java.io.File
import java.util.stream.Collectors


internal object VoiceLogger {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val creator = MessageCreator(File(PLUGIN_DIR_FILE, "lang"), DEFAULT_LOCALE, COMPONENT_PREFIX)

    internal fun setting(event: SlashCommandInteractionEvent) = event.hook.editOriginal(
        getSettingMenu(
            dataMap.computeIfAbsent(event.channelIdLong) { ChannelData(event.guild!!) },
            event.userLocale,
            Placeholder.getSubstitutor(event)
        )
    ).queue()

    internal fun onToggle(event: ButtonInteractionEvent) {
        // update
        val channelData = JsonManager.toggle(event.guild!!, event.channel.idLong)

        // reply
        event.hook.editOriginal(
            getSettingMenu(
                channelData,
                event.userLocale,
                Placeholder.getSubstitutor(event)
            )
        ).queue()
        event.deferEdit().queue()
    }

    internal fun onDelete(event: ButtonInteractionEvent) {
        // update
        JsonManager.delete(event.guild!!.idLong, event.channel.idLong)

        // reply
        event.editMessage(
            MessageEditData.fromCreateData(
                creator.getCreateBuilder(
                    "delete",
                    event.userLocale,
                    Placeholder.getSubstitutor(event)
                ).build()
            )
        ).queue()
    }

    internal fun onSelect(event: EntitySelectInteractionEvent) {
        // update
        val guild = event.guild!!
        val channelIds = event.values
            .stream()
            .map { obj: IMentionable -> obj.idLong }
            .collect(Collectors.toList())
        val channelData = when (event.componentId.removePrefix(COMPONENT_PREFIX)) {
            "modify_allow" -> {
                JsonManager.addAllowChannels(
                    guild = guild,
                    listenChannelId = event.channelIdLong,
                    detectedChannelIds = channelIds
                )
            }

            "modify_block" -> {
                JsonManager.addBlockChannels(
                    guild = guild,
                    listenChannelId = event.channelIdLong,
                    detectedChannelIds = channelIds
                )
            }

            else -> throw Exception("Unknown key ${event.componentId.removePrefix(COMPONENT_PREFIX)}")
        }

        // reply
        event.hook.editOriginal(
            getSettingMenu(
                channelData,
                event.userLocale,
                Placeholder.getSubstitutor(event)
            )
        ).queue()
        event.deferEdit().queue()
    }

    internal fun createSel(event: ButtonInteractionEvent, componentId: String) {
        event.editMessage(
            MessageEditData.fromCreateData(
                creator.getCreateBuilder(
                    componentId,
                    event.userLocale, Placeholder.getSubstitutor(event)
                ).build()
            )
        ).queue()
    }


    fun onChannelStatusNew(event: ChannelUpdateVoiceStatusEvent, data: StatusEventData) {
        val listenChannelIds: List<Long> = dataMap.entries
            .filter { (_, value) -> data.channel in value.getCurrentDetectChannels() }
            .map { (key, _) -> key }
        if (listenChannelIds.isEmpty()) return

        data.member.queue { member ->
            val substitutor = Placeholder.getSubstitutor(member).putAll(
                "vl_category_mention" to data.channel.parentCategory!!.asMention,
                "vl_channel_mention" to data.channel.asMention,
                "vl_channel_url" to data.channel.jumpUrl,
                "vl_status_after" to data.newStr!!,
            )
            sendListenChannel("on-status-new", event.guild, listenChannelIds, substitutor)
        }
    }

    fun onChannelStatusUpdate(event: ChannelUpdateVoiceStatusEvent, data: StatusEventData) {
        val listenChannelIds: List<Long> = dataMap.entries
            .filter { (_, value) -> data.channel in value.getCurrentDetectChannels() }
            .map { (key, _) -> key }
        if (listenChannelIds.isEmpty()) return

        data.member.queue { member ->
            val substitutor = Placeholder.getSubstitutor(member).putAll(
                "vl_category_mention" to data.channel.parentCategory!!.asMention,
                "vl_channel_mention" to data.channel.asMention,
                "vl_channel_url" to data.channel.jumpUrl,
                "vl_status_before" to data.oldStr!!,
                "vl_status_after" to data.newStr!!,
            )
            sendListenChannel("on-status-update", event.guild, listenChannelIds, substitutor)
        }
    }

    fun onChannelStatusDelete(event: ChannelUpdateVoiceStatusEvent, data: StatusEventData) {
        val listenChannelIds: List<Long> = dataMap.entries
            .filter { (_, value) -> data.channel in value.getCurrentDetectChannels() }
            .map { (key, _) -> key }
        if (listenChannelIds.isEmpty()) return

        data.member.queue { member ->
            val substitutor = Placeholder.getSubstitutor(member).putAll(
                "vl_category_mention" to data.channel.parentCategory!!.asMention,
                "vl_channel_mention" to data.channel.asMention,
                "vl_channel_url" to data.channel.jumpUrl,
                "vl_status_before" to data.oldStr!!,
            )
            sendListenChannel("on-status-delete", event.guild, listenChannelIds, substitutor)
        }
    }

    fun onChannelJoin(event: GuildVoiceUpdateEvent, data: VoiceEventData) {
        val listenChannelIds: List<Long> = dataMap.entries
            .filter { (_, value) -> data.channelJoin!! in value.getCurrentDetectChannels() }
            .map { (key, _) -> key }
        if (listenChannelIds.isEmpty()) return

        val substitutor = Placeholder.getSubstitutor(data.member).putAll(
            "vl_category_join_mention" to data.channelJoin!!.parentCategory!!.asMention,
            "vl_channel_join_mention" to data.channelJoin.asMention,
            "vl_channel_join_url" to data.channelJoin.jumpUrl,
        )
        sendListenChannel("on-channel-join", event.guild, listenChannelIds, substitutor)
    }

    fun onChannelSwitch(event: GuildVoiceUpdateEvent, data: VoiceEventData) {
        val listenChannelIds: List<Long> = dataMap.entries
            .filter { (_, value) ->
                (data.channelJoin!! in value.getCurrentDetectChannels()) or
                        (data.channelLeft!! in value.getCurrentDetectChannels())
            }
            .map { (key, _) -> key }
        if (listenChannelIds.isEmpty()) return

        val substitutor = Placeholder.getSubstitutor(data.member).putAll(
            "vl_category_join_mention" to data.channelJoin!!.parentCategory!!.asMention,
            "vl_channel_join_mention" to data.channelJoin.asMention,
            "vl_channel_join_url" to data.channelJoin.jumpUrl,
            "vl_category_left_mention" to data.channelLeft!!.parentCategory!!.asMention,
            "vl_channel_left_mention" to data.channelLeft.asMention,
            "vl_channel_left_url" to data.channelLeft.jumpUrl,
        )
        sendListenChannel("on-channel-switch", event.guild, listenChannelIds, substitutor)
    }

    fun onChannelLeft(event: GuildVoiceUpdateEvent, data: VoiceEventData) {
        val listenChannelIds: List<Long> = dataMap.entries
            .filter { (_, value) -> data.channelLeft!! in value.getCurrentDetectChannels() }
            .map { (key, _) -> key }
        if (listenChannelIds.isEmpty()) return

        val substitutor = Placeholder.getSubstitutor(data.member).putAll(
            "vl_category_left_mention" to data.channelLeft!!.parentCategory!!.asMention,
            "vl_channel_left_mention" to data.channelLeft.asMention,
            "vl_channel_left_url" to data.channelLeft.jumpUrl,
        )
        sendListenChannel("on-channel-left", event.guild, listenChannelIds, substitutor)
    }

    private fun sendListenChannel(key: String, guild: Guild, listenChannelId: List<Long>, substitutor: Substitutor) {
        val message = creator.getCreateBuilder(key, guild.locale, substitutor).build()

        listenChannelId.forEach {
            val listenChannel = guild.getGuildChannelById(it) ?: return

            when (listenChannel) {
                is TextChannel -> listenChannel.sendMessage(message).queue()
                is VoiceChannel -> listenChannel.sendMessage(message).queue()
                else -> throw Exception("Unknown channel type")
            }
        }
    }

    internal data class StatusEventData(
        val guildId: Long,
        val locale: DiscordLocale,
        val channel: VoiceChannel,
        val member: CacheRestAction<Member>,
        val oldStr: String?,
        val newStr: String?,
    )

    internal data class VoiceEventData(
        val guildId: Long,
        val locale: DiscordLocale,
        val member: Member,
        val channelJoin: AudioChannel?,
        val channelLeft: AudioChannel?,
    )

    private fun getSettingMenu(
        channelData: ChannelData,
        locale: DiscordLocale,
        substitutor: Substitutor
    ): MessageEditData {
        val allowListFormat = PlaceholderLocalizations.allowListFormat[locale]
        val blockListFormat = PlaceholderLocalizations.blockListFormat[locale]

        val allowString = StringBuilder().apply {
            if (!channelData.getAllowArray().isEmpty) channelData.getAllowArray().map { it.asString }
                .forEach { detectedChannelId ->
                    append(
                        substitutor.parse(
                            allowListFormat
                                .replace("%allowlist_channel_mention%", "<#${detectedChannelId}>")
                                .replace("%allowlist_channel_id%", detectedChannelId)
                        )
                    )
                } else {
                append(substitutor.parse(PlaceholderLocalizations.empty[locale]))
            }
        }.toString()

        val blockString = StringBuilder().apply {
            if (!channelData.getBlockArray().isEmpty) channelData.getBlockArray().map { it.asString }
                .forEach { detectedChannelId ->
                    append(
                        substitutor.parse(
                            blockListFormat
                                .replace("%blocklist_channel_mention%", "<#${detectedChannelId}>")
                                .replace("%blocklist_channel_id%", detectedChannelId)
                        )
                    )
                } else {
                append(substitutor.parse(PlaceholderLocalizations.empty[locale]))
            }
        }.toString()

        substitutor.apply {
            putAll(
                "vl_channel_mode" to if (channelData.getChannelMode()) "ALLOW" else "BLOCK",
                "vl_allow_list_format" to allowString,
                "vl_block_list_format" to blockString
            )
        }


        return MessageEditData.fromCreateData(
            creator.getCreateBuilder("voice-logger@setting", locale, substitutor).build()
        )
    }
}