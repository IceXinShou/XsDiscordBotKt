package tw.xserver.plugin.logger.chat

import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.plugin.creator.message.MessageCreator
import tw.xserver.plugin.logger.chat.Event.COMPONENT_PREFIX
import tw.xserver.plugin.logger.chat.Event.DIR_PATH
import tw.xserver.plugin.logger.chat.lang.PlaceholderLocalizations
import tw.xserver.plugin.placeholder.Placeholder
import tw.xserver.plugin.placeholder.Substitutor
import java.util.stream.Collectors

object ChatLogger {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val messageCreator = MessageCreator(DIR_PATH)

    fun setting(event: SlashCommandInteractionEvent) = event.hook.editOriginal(
        getSettingMenu(
            JsonManager.getOrDefault(event.guild!!.idLong, event.channel.idLong),
            event.userLocale,
            Placeholder.get(event)
        ).build()
    ).queue()

    fun toggle(event: ButtonInteractionEvent) = event.hook.editOriginal(
        getSettingMenu(
            JsonManager.getOrDefault(event.guild!!.idLong, event.channel.idLong).toggle(),
            event.userLocale,
            Placeholder.get(event)
        ).build()
    ).queue()

    fun delete(event: ButtonInteractionEvent) {
        JsonManager.delete(event.guild!!.idLong, event.channel.idLong)

        event.editMessage(
            messageCreator.getBuilder(
                "delete",
                event.userLocale,
                Placeholder.get(event)
            ).build()
        ).queue()
    }

    fun select(event: EntitySelectInteractionEvent) {
        val guild = event.guild!!
        val channelMode: ChannelSetting.ChannelMode = when (event.componentId.removePrefix(COMPONENT_PREFIX)) {
            "modify_allow" -> ChannelSetting.ChannelMode.Allow
            "modify_block" -> ChannelSetting.ChannelMode.Block
            else -> throw Exception("Unknown key ${event.componentId.removePrefix(COMPONENT_PREFIX)}")
        }
        val channelIds = event.values
            .stream()
            .map { obj: IMentionable -> obj.idLong }
            .collect(Collectors.toList())

        val setting = JsonManager.addChannels(
            guildId = guild.idLong,
            rootId = event.channelIdLong,
            channelIds = channelIds,
            channelMode = channelMode
        )

        event.hook.editOriginal(
            getSettingMenu(
                setting,
                event.userLocale,
                Placeholder.get(event)
            ).build()
        ).queue()
    }

    fun createSel(event: ButtonInteractionEvent) {
        event.editMessage(
            messageCreator.getBuilder(event.componentId, event.userLocale, Placeholder.get(event)).build()
        ).queue()
    }

    private fun getSettingMenu(
        setting: ChannelSetting,
        locale: DiscordLocale,
        substitutor: Substitutor
    ): MessageEditBuilder {
        val allowListFormat = PlaceholderLocalizations.allowListFormat[locale]
        val blockListFormat = PlaceholderLocalizations.blockListFormat[locale]
        val allowBuilder = StringBuilder().apply {
            if (setting.allow.isNotEmpty()) setting.allow.forEach { i ->
                append(
                    substitutor.parse(
                        allowListFormat
                            .replace("%allowlist_channel_mention%", "<#${i.detectId}>")
                            .replace("%allowlist_channel_id%", i.detectId.toString())
                    )
                )
            } else {
                append(substitutor.parse(PlaceholderLocalizations.empty[locale]))
            }
        }

        val blockBuilder = StringBuilder().apply {
            if (setting.block.isNotEmpty()) setting.block.forEach { i ->
                append(
                    substitutor.parse(
                        blockListFormat
                            .replace("%blocklist_channel_mention%", "<#${i.detectId}>")
                            .replace("%blocklist_channel_id%", i.detectId.toString())
                    )
                )
            } else {
                append(substitutor.parse(PlaceholderLocalizations.empty[locale]))
            }
        }


        val messageData = messageCreator.getMessageData("chat-logger:setting", locale)
        messageData.embeds.forEach { embed ->
            embed.fields.forEach { field ->
                when (field.value) {
                    "%allow_list_format%" -> field.value = allowBuilder.toString()
                    "%block_list_format%" -> field.value = blockBuilder.toString()
                }
            }
        }

        substitutor.apply {
            put("cl_channel_mode" to setting.channelMode.name.lowercase())
        }

        return messageCreator.getBuilder(messageData, substitutor)
    }
}