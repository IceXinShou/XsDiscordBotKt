package tw.xserver.plugin.creator.message

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.dv8tion.jda.internal.interactions.component.ButtonImpl
import tw.xserver.plugin.creator.message.setting.MessageDataSerializer
import tw.xserver.plugin.placeholder.PAPI
import tw.xserver.plugin.placeholder.Substitutor
import java.io.File
import java.util.*

class CreatorImpl(langPath: String) {
    private val localeMapper: MutableMap<DiscordLocale, MutableMap<String, MessageDataSerializer>> =
        EnumMap(DiscordLocale::class.java)

    init {
        File(langPath).listFiles()?.filter { it.isDirectory }?.forEach { directory ->
            val locale = DiscordLocale.from(directory.name)
            val messagesMap = localeMapper.getOrPut(locale) { mutableMapOf() }

            File(directory, "./message/").listFiles()?.filter { it.isFile && it.extension == "yml" }
                ?.forEach { file ->
                    messagesMap[file.nameWithoutExtension] =
                        Yaml().decodeFromString<MessageDataSerializer>(file.readText())
                }
        }
    }

    fun getBuilder(
        locale: DiscordLocale,
        commandMethod: String,
        substitutor: Substitutor = PAPI.globalPlaceholder
    ): MessageEditBuilder {
        val messageData = getMessageData(locale, commandMethod)
        val builder = MessageEditBuilder()

        messageData.content.let { builder.setContent(substitutor.parse(it)) }
        messageData.embeds.let { embeds ->
            builder.setEmbeds(MessageCreator.buildEmbeds(embeds, substitutor))
        }

        val components: MutableList<LayoutComponent> = ArrayList()
        messageData.components.forEach { component ->
            when (component) {
                is MessageDataSerializer.Component.ButtonsComponent -> {
                    components.add(ActionRow.of(component.buttons.map { button ->
                        ButtonImpl(
                            button.uidOrUrl,
                            button.label,
                            when (button.style) {
                                1 -> ButtonStyle.PRIMARY
                                2 -> ButtonStyle.SECONDARY
                                3 -> ButtonStyle.SUCCESS
                                4 -> ButtonStyle.DANGER
                                5 -> ButtonStyle.LINK
                                else -> throw IllegalArgumentException("Unknown style code: ${button.style}")

                            },
                            button.disabled,
                            button.emoji?.let { Emoji.fromUnicode(button.emoji.name) }
                        )
                    }))
                }

                is MessageDataSerializer.Component.StringSelectMenuSetting -> {
                    val menu = StringSelectMenu.create(component.uid).apply {
                        placeholder = component.placeholder
                        minValues = component.min
                        maxValues = component.max
                        component.options.forEach { option ->
                            addOption(
                                option.label,
                                option.value,
                                option.description,
                                option.emoji?.let { Emoji.fromUnicode(option.emoji.name) }
                            )
                        }
                    }

                    components.add(ActionRow.of(menu.build()))
                }

                is MessageDataSerializer.Component.EntitySelectMenuSetting -> {
                    val menu =
                        EntitySelectMenu.create(
                            component.uid,
                            EntitySelectMenu.SelectTarget.valueOf(component.selectTargetType.uppercase())
                        ).apply {
                            placeholder = component.placeholder
                            minValues = component.min
                            maxValues = component.max
                        }
                    if (component.selectTargetType.uppercase() == "CHANNEL") {
                        if (component.channelTypes.isEmpty()) {
                            throw IllegalArgumentException("'channel_types' cannot be empty when 'select_target_type' is set to 'CHANNEL'!")
                        }
                        menu.setChannelTypes(component.channelTypes.map { ChannelType.valueOf(it) })
                    }

                    components.add(ActionRow.of(menu.build()))
                }
            }
        }

        builder.setComponents(components)

        return builder
    }

    fun getMessageData(locale: DiscordLocale, commandMethod: String): MessageDataSerializer =
        localeMapper[locale]?.get(commandMethod)!!
}
