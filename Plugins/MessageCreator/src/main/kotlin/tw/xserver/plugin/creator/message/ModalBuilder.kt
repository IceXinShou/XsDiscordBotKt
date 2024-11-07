package tw.xserver.plugin.creator.message

import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.modals.Modal
import tw.xserver.loader.builtin.placeholder.Placeholder
import tw.xserver.loader.builtin.placeholder.Substitutor
import tw.xserver.plugin.creator.message.serializer.ModalDataSerializer
import java.util.*

open class ModalBuilder(private val componentPrefix: String, private val defaultLocale: DiscordLocale) {
    protected val modalLocaleMapper: MutableMap<DiscordLocale, MutableMap<String, ModalDataSerializer>> =
        EnumMap(DiscordLocale::class.java)

    fun getModalBuilder(
        key: String,
        locale: DiscordLocale,
        substitutor: Substitutor = Placeholder.globalPlaceholder,
    ): Modal.Builder {
        return getModalBuilder(getModalData(key, locale), substitutor)
    }

    fun getModalData(key: String, locale: DiscordLocale): ModalDataSerializer =
        modalLocaleMapper.getOrDefault(locale, modalLocaleMapper[defaultLocale])
            ?.get(key.removePrefix(componentPrefix))
            ?: throw IllegalStateException("Message data not found for command: $key")

    protected fun getModalBuilder(
        modalData: ModalDataSerializer,
        substitutor: Substitutor = Placeholder.globalPlaceholder,
    ): Modal.Builder {
        val builder = Modal.create(
            substitutor.parse("$componentPrefix${substitutor.parse(modalData.customId)}"),
            substitutor.parse(substitutor.parse(modalData.title))
        ).apply {
            modalData.textInputs.forEach { textInput ->
                components.add(
                    ActionRow.of(
                        TextInput.create(
                            substitutor.parse(textInput.uid),
                            substitutor.parse(textInput.label),
                            textInput.style
                        ).apply {
                            value = textInput.value?.let { substitutor.parse(it) }
                            placeholder = textInput.placeholder?.let { substitutor.parse(it) }
                            minLength = textInput.minLength
                            maxLength = textInput.maxLength
                            isRequired = textInput.required
                        }.build()
                    )
                )
            }
        }
        return builder
    }
}
