package tw.xserver.plugin.creator.message

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import tw.xserver.plugin.creator.message.setting.MessageData
import tw.xserver.plugin.placeholder.Substitutor
import java.io.File
import java.util.*

class CreatorImpl(langPath: String) {
    private val localeMapper: MutableMap<DiscordLocale, MutableMap<String, MessageData>> =
        EnumMap(DiscordLocale::class.java)

    init {
        File(langPath).listFiles()?.filter { it.isDirectory }?.forEach { directory ->
            val locale = DiscordLocale.from(directory.name)
            val messagesMap = localeMapper.getOrPut(locale) { mutableMapOf() }

            File(directory, "./message/").listFiles()?.filter { it.isFile && it.extension == "yml" }
                ?.forEach { file ->
                    messagesMap[file.nameWithoutExtension] =
                        Yaml().decodeFromString<MessageData>(file.readText())
                }
        }
    }

    fun getBuilder(locale: DiscordLocale, commandMethod: String, substitutor: Substitutor): MessageEditBuilder {
        val messageData = getMessageData(locale, commandMethod)
        val builder = MessageEditBuilder()

        messageData.content.let { builder.setContent(substitutor.parse(it)) }
        messageData.embeds.let { embeds ->
            builder.setEmbeds(MessageCreator.buildEmbeds(embeds, substitutor))
        }

        return builder
    }

    fun getMessageData(locale: DiscordLocale, commandMethod: String): MessageData =
        localeMapper[locale]?.get(commandMethod)!!
}
