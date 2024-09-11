package tw.xserver.plugin.economy

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageEditData
import tw.xserver.plugin.creator.message.MessageCreator
import tw.xserver.plugin.economy.Event.PLUGIN_DIR_FILE
import tw.xserver.plugin.economy.Event.storageManager
import tw.xserver.plugin.placeholder.Placeholder
import java.io.File

internal object MessageReplier {
    private val creator = MessageCreator(File(PLUGIN_DIR_FILE, "lang"))

    fun reply(event: SlashCommandInteractionEvent): MessageEditData =
        creator.getEditBuilder(event, event.user.let { Placeholder.getSubstitutor(it) }).build()

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
                creator.getMessageData(event).embeds[0].fields[0],
                substitutor
            ).build()
        )

        return builder.build()
    }
}