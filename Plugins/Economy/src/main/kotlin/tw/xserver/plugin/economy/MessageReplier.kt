package tw.xserver.plugin.economy

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageEditData
import tw.xserver.plugin.creator.message.MessageCreator
import tw.xserver.plugin.economy.Event.DIR_PATH
import tw.xserver.plugin.economy.storage.JsonManager
import tw.xserver.plugin.economy.storage.SheetManager
import tw.xserver.plugin.placeholder.Placeholder

internal object MessageReplier {
    private val creator = MessageCreator("$DIR_PATH./lang/")

    fun reply(event: SlashCommandInteractionEvent): MessageEditData =
        creator.getBuilder(event, event.user.let { Placeholder.get(it) }).build()

    fun replyBoard(
        user: User,
        event: SlashCommandInteractionEvent,
        type: Economy.Type,
        mode: Economy.Mode,
    ): MessageEditData {
        val substitutor = user.let { Placeholder.get(it) }
        val builder = creator.getBuilder(event, substitutor)

        builder.setEmbeds(
            if (mode == Economy.Mode.Json) {
                JsonManager.getEmbedBuilder(
                    type,
                    EmbedBuilder(builder.embeds[0]),
                    creator.getMessageData(event).embeds[0].fields[0],
                    substitutor
                )
            } else {
                SheetManager.getEmbedBuilder(
                    type,
                    EmbedBuilder(builder.embeds[0]),
                    creator.getMessageData(event).embeds[0].fields[0],
                    substitutor
                )
            }.build()
        )

        return builder.build()
    }
}