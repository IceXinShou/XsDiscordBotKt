package tw.xserver.plugin.economy

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageEditData
import tw.xserver.loader.builtin.placeholder.Placeholder
import tw.xserver.plugin.creator.message.MessageCreator
import tw.xserver.plugin.economy.Event.COMPONENT_PREFIX
import tw.xserver.plugin.economy.Event.PLUGIN_DIR_FILE
import tw.xserver.plugin.economy.Event.storageManager
import java.io.File

internal object MessageReplier {
    private val creator = MessageCreator(File(PLUGIN_DIR_FILE, "lang"), COMPONENT_PREFIX)

    fun reply(event: GenericInteractionCreateEvent): MessageEditData =
        creator.getEditBuilder(
            event, event.user.let { Placeholder.getSubstitutor(it) }
        ).build()

    fun replyBoard(
        event: SlashCommandInteractionEvent,
        type: Economy.Type,
    ): MessageEditData {
        val substitutor = event.user.let { Placeholder.getSubstitutor(it) }
        val builder = creator.getEditBuilder(event, substitutor)

        builder.setEmbeds(
            storageManager.getEmbedBuilder(
                type,
                EmbedBuilder(builder.embeds[0]),
                creator.getMessageData(event).embeds[0].description!!,
                substitutor
            ).build()
        )

        return builder.build()
    }
}