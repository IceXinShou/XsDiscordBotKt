package tw.xserver.plugin.creator.message

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import net.dv8tion.jda.api.interactions.DiscordLocale
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.plugin.creator.message.serializer.ModalDataSerializer
import tw.xserver.plugin.creator.message.serializer.TextInputStyleSerializer
import java.io.File

class ModalCreator(langDirFile: File, defaultLocale: DiscordLocale, componentPrefix: String = "") :
    ModalBuilder(componentPrefix, defaultLocale) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    init {
        val yaml = Yaml(
            serializersModule = SerializersModule {
                contextual(TextInputStyleSerializer)
            }
        )


        langDirFile.listFiles()?.filter { it.isDirectory }?.forEach { directory ->
            val locale = DiscordLocale.from(directory.name)

            File(directory, "./modal/").listFiles()?.filter { it.isFile && it.extension in listOf("yml", "yaml") }
                ?.forEach { file ->
                    modalLocaleMapper.getOrPut(locale) { mutableMapOf() }[file.nameWithoutExtension] =
                        yaml.decodeFromString<ModalDataSerializer>(file.readText())
                    logger.debug(
                        "Added modal {} | {}: {}",
                        langDirFile.parentFile.nameWithoutExtension,
                        directory.name,
                        file.nameWithoutExtension
                    )
                }
        }
    }
}
