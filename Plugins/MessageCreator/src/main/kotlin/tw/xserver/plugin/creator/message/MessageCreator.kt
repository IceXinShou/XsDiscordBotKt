package tw.xserver.plugin.creator.message

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.dv8tion.jda.internal.interactions.component.ButtonImpl
import org.apache.commons.lang3.StringUtils.isNumeric
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.plugin.creator.message.serializer.MessageDataSerializer
import tw.xserver.plugin.creator.message.serializer.MessageDataSerializer.EmbedSetting
import tw.xserver.plugin.creator.message.serializer.list.ColorSerializer
import tw.xserver.plugin.placeholder.Placeholder
import tw.xserver.plugin.placeholder.Substitutor
import java.io.File
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

class MessageCreator(langDirFile: File, private val componentPrefix: String = "") {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    private val localeMapper: MutableMap<DiscordLocale, MutableMap<String, MessageDataSerializer>> =
        EnumMap(DiscordLocale::class.java)

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


    fun getEditBuilder(
        event: SlashCommandInteractionEvent,
        substitutor: Substitutor = Placeholder.globalPlaceholder,
    ): MessageEditBuilder {
        return getEditBuilder(getMessageData(event), substitutor)
    }

    fun getEditBuilder(
        key: String,
        locale: DiscordLocale,
        substitutor: Substitutor = Placeholder.globalPlaceholder,
    ): MessageEditBuilder {
        return getEditBuilder(getMessageData(key, locale), substitutor)
    }

    fun getEditBuilder(
        messageData: MessageDataSerializer,
        substitutor: Substitutor = Placeholder.globalPlaceholder,
    ): MessageEditBuilder {
        val builder = MessageEditBuilder()

        messageData.content.let { builder.setContent(substitutor.parse(it)) }
        messageData.embeds.let { embeds ->
            builder.setEmbeds(buildEmbeds(embeds, substitutor))
        }

        val components: MutableList<LayoutComponent> = ArrayList()
        messageData.components.forEach { component ->
            when (component) {
                is MessageDataSerializer.Component.ButtonsComponent -> {
                    components.add(ActionRow.of(component.buttons.map { button ->
                        ButtonImpl(
                            "$componentPrefix${substitutor.parse(button.uidOrUrl)}",
                            button.label?.let { substitutor.parse(it) },
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
                    val menu = StringSelectMenu
                        .create("$componentPrefix${substitutor.parse(component.uid)}").apply {
                            placeholder = component.placeholder?.let { substitutor.parse(it) }
                            minValues = component.min
                            maxValues = component.max
                            component.options.forEach { option ->
                                addOption(
                                    substitutor.parse(option.label),
                                    substitutor.parse(option.value),
                                    option.description?.let { substitutor.parse(it) },
                                    option.emoji?.let { Emoji.fromUnicode(option.emoji.name) }
                                )
                            }
                        }

                    components.add(ActionRow.of(menu.build()))
                }

                is MessageDataSerializer.Component.EntitySelectMenuSetting -> {
                    val menu =
                        EntitySelectMenu.create(
                            "$componentPrefix${substitutor.parse(component.uid)}",
                            EntitySelectMenu.SelectTarget.valueOf(component.selectTargetType.uppercase())
                        ).apply {
                            placeholder = component.placeholder?.let { substitutor.parse(it) }
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

    fun getMessageData(event: SlashCommandInteractionEvent): MessageDataSerializer {
        return getMessageData(parseCommandName(event), event.userLocale)
    }

    fun getMessageData(key: String, locale: DiscordLocale): MessageDataSerializer =
        localeMapper[locale]?.get(key.removePrefix(componentPrefix))
            ?: throw IllegalStateException("Message data not found for command: $key")

    fun buildEmbeds(embeds: List<EmbedSetting>, substitutor: Substitutor? = null): List<MessageEmbed> =
        embeds.mapNotNull { embed -> buildEmbed(embed, substitutor) }

    private fun buildEmbed(embed: EmbedSetting, substitutor: Substitutor?): MessageEmbed? {
        val builder = EmbedBuilder()

        // Set author once using substitutor if available or directly if not
        embed.author?.let { author ->
            builder.setAuthor(
                substitutor?.parse(author.name) ?: author.name,
                author.url?.let { substitutor?.parse(it) } ?: author.url,
                author.iconUrl?.let { substitutor?.parse(it) } ?: author.iconUrl
            )
        }

        // Handle title, description, thumbnail, and image with or without substitutor
        if (substitutor != null) {
            embed.title?.let { title ->
                builder.setTitle(substitutor.parse(title.text), title.url?.let { substitutor.parse(it) })
            }
            embed.description?.let { desc -> builder.setDescription(substitutor.parse(desc)) }
            embed.thumbnailUrl?.let { url -> builder.setThumbnail(substitutor.parse(url)) }
            embed.imageUrl?.let { url -> builder.setImage(substitutor.parse(url)) }
        } else {
            embed.title?.let { title -> builder.setTitle(title.text, title.url) }
            embed.description?.let { desc -> builder.setDescription(desc) }
            embed.thumbnailUrl?.let { url -> builder.setThumbnail(url) }
            embed.imageUrl?.let { url -> builder.setImage(url) }
        }

        // Apply color and timestamp directly since they don't involve parsing
        embed.colorCode.let { builder.setColor(it) }

        embed.timestamp?.let {
            when {
                isNumeric(it) -> Instant.ofEpochMilli(it.toLong())
                it == "%now%" -> OffsetDateTime.now().toInstant()
                else -> throw Exception("Unknown format for timestamp!")
            }
        }

        // Set footer similarly to author
        embed.footer?.let { footer ->
            builder.setFooter(
                substitutor?.parse(footer.text) ?: footer.text,
                footer.iconUrl?.let { substitutor?.parse(it) } ?: footer.iconUrl
            )
        }

        // Handle fields, applying substitutor if available
        embed.fields.forEach { field ->
            builder.addField(
                substitutor?.parse(field.name) ?: field.name,
                substitutor?.parse(field.value) ?: field.value,
                field.inline
            )
        }

        // Build the embed only if it's not empty
        return if (builder.isEmpty) null else builder.build()
    }

    private fun parseCommandName(event: SlashCommandInteractionEvent): String {
        return "${event.name}${event.subcommandName?.let { "@$it" } ?: ""}"
    }
}
