package tw.xserver.plugin.creator.message

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.builtin.placeholder.Placeholder
import tw.xserver.loader.builtin.placeholder.Substitutor
import tw.xserver.plugin.creator.message.serializer.MessageDataSerializer
import tw.xserver.plugin.creator.message.serializer.list.ColorSerializer
import java.io.File

class MessageCreator(langDirFile: File, defaultLocale: DiscordLocale, componentPrefix: String = "") :
    Builder(componentPrefix, defaultLocale) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    init {
        val yaml = Yaml(
            serializersModule = SerializersModule {
                contextual(ColorSerializer)
                include(SerializersModule {
                    polymorphic(MessageDataSerializer.Component::class) {
                        subclass(MessageDataSerializer.Component.ButtonsComponent::class)
                        subclass(MessageDataSerializer.Component.StringSelectMenuSetting::class)
                        subclass(MessageDataSerializer.Component.EntitySelectMenuSetting::class)
                    }
                })
            }
        )

        langDirFile.listFiles()?.filter { it.isDirectory }?.forEach { directory ->
            val locale = DiscordLocale.from(directory.name)

            File(directory, "./message/").listFiles()?.filter { it.isFile && it.extension == "yml" }
                ?.forEach { file ->
                    localeMapper.getOrPut(locale) { mutableMapOf() }[file.nameWithoutExtension] =
                        yaml.decodeFromString<MessageDataSerializer>(file.readText())
                    logger.debug(
                        "Added message {} | {}: {}",
                        langDirFile.parentFile.nameWithoutExtension,
                        directory.name,
                        file.nameWithoutExtension
                    )
                }
        }
    }

    fun getMessageData(event: CommandInteractionPayload): MessageDataSerializer {
        return getMessageData(parseCommandName(event), event.userLocale)
    }

    fun getMessageData(event: ComponentInteraction): MessageDataSerializer {
        return getMessageData(parseCommandName(event), event.userLocale)
    }

    fun getCreateBuilder(
        event: CommandInteractionPayload,
        substitutor: Substitutor = Placeholder.globalPlaceholder,
    ): MessageCreateBuilder {
        return getCreateBuilder(getMessageData(event), substitutor)
    }

    fun getCreateBuilder(
        event: ComponentInteraction,
        substitutor: Substitutor = Placeholder.globalPlaceholder,
    ): MessageCreateBuilder {
        return getCreateBuilder(getMessageData(event), substitutor)
    }
}
