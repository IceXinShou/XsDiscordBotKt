package tw.xserver.plugin.economy

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.utils.messages.MessageEditData
import tw.xserver.plugin.creator.message.CreatorImpl
import tw.xserver.plugin.economy.Economy.DIR_PATH
import tw.xserver.plugin.economy.googlesheet.SheetManager
import tw.xserver.plugin.economy.json.JsonManager
import tw.xserver.plugin.placeholder.PAPI

internal object MessageReplier {
    private val creator = CreatorImpl("$DIR_PATH./lang/")

    fun reply(user: User, cmd: Economy.InteractType, locale: DiscordLocale): MessageEditData =
        creator.getBuilder(locale, cmd.value, user.let { PAPI.get(it) }).build()

    fun replyBoard(
        user: User,
        cmd: Economy.InteractType,
        type: Economy.Type,
        mode: Economy.Mode,
        locale: DiscordLocale
    ): MessageEditData {
        val substitutor = user.let { PAPI.get(it) }
        val builder = creator.getBuilder(locale, cmd.value, substitutor)

        builder.setEmbeds(
            if (mode == Economy.Mode.Json) {
                JsonManager.getEmbedBuilder(
                    type,
                    EmbedBuilder(builder.embeds[0]),
                    creator.getMessageData(locale, cmd.value).embeds[0].fields[0],
                    substitutor
                )
            } else {
                SheetManager.getEmbedBuilder(
                    type,
                    EmbedBuilder(builder.embeds[0]),
                    creator.getMessageData(locale, cmd.value).embeds[0].fields[0],
                    substitutor
                )
            }.build()
        )

        return builder.build()
    }
}