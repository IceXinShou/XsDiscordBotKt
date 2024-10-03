package tw.xserver.plugin.economy

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import net.dv8tion.jda.api.utils.messages.MessageEditData
import tw.xserver.loader.builtin.placeholder.Placeholder
import tw.xserver.loader.builtin.placeholder.Substitutor
import tw.xserver.plugin.creator.message.MessageCreator
import tw.xserver.plugin.economy.Event.COMPONENT_PREFIX
import tw.xserver.plugin.economy.Event.PLUGIN_DIR_FILE
import tw.xserver.plugin.economy.Event.storageManager
import java.io.File

internal object MessageReplier {
    private val creator = MessageCreator(File(PLUGIN_DIR_FILE, "lang"), DiscordLocale.CHINESE_TAIWAN, COMPONENT_PREFIX)
    fun getNoPermissionMessageData(
        locale: DiscordLocale,
    ): MessageEditData =
        MessageEditData.fromCreateData(
            creator.getCreateBuilder("no-permission", locale).build()
        )

    fun getMessageEditData(
        interactionPayload: CommandInteractionPayload,
        locale: DiscordLocale,
        substitutor: Substitutor
    ): MessageEditData =
        MessageEditData.fromCreateData(
            creator.getCreateBuilder(
                creator.parseCommandName(interactionPayload), locale, substitutor
            ).build()
        )

    fun getMessageEditData(
        componentInteraction: ComponentInteraction,
        locale: DiscordLocale,
        substitutor: Substitutor
    ): MessageEditData =
        MessageEditData.fromCreateData(
            creator.getCreateBuilder(
                creator.parseCommandName(componentInteraction), locale, substitutor
            ).build()
        )

    fun replyBoard(
        event: SlashCommandInteractionEvent,
        type: Economy.Type,
    ): MessageEditData {
        val substitutor = event.user.let { Placeholder.getSubstitutor(it) }
        val builder = creator.getCreateBuilder(event, substitutor)

        builder.setEmbeds(
            storageManager.getEmbedBuilder(
                type,
                EmbedBuilder(builder.embeds[0]),
                creator.getMessageData(event).embeds[0].description!!,
                substitutor
            ).build()
        )

        return MessageEditData.fromCreateData(builder.build())
    }
}