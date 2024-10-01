package tw.xserver.plugin.creator.message

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.internal.interactions.component.ButtonImpl
import org.apache.commons.lang3.StringUtils.isNumeric
import tw.xserver.loader.builtin.placeholder.Placeholder
import tw.xserver.loader.builtin.placeholder.Substitutor
import tw.xserver.plugin.creator.message.serializer.MessageDataSerializer
import tw.xserver.plugin.creator.message.serializer.MessageDataSerializer.EmbedSetting
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

open class Builder(private val componentPrefix: String, private val defaultLocale: DiscordLocale) {
    protected val localeMapper: MutableMap<DiscordLocale, MutableMap<String, MessageDataSerializer>> =
        EnumMap(DiscordLocale::class.java)

    protected open fun parseCommandName(event: GenericInteractionCreateEvent): String {
        return when (event) {
            is SlashCommandInteractionEvent ->
                "${event.name}${event.subcommandName?.let { "@$it" } ?: ""}"

            is ButtonInteractionEvent ->
                event.componentId.removePrefix(componentPrefix)

            else -> throw IllegalArgumentException("Unsupported event type")
        }
    }

    protected fun getMessageData(key: String, locale: DiscordLocale): MessageDataSerializer =
        localeMapper.getOrDefault(locale, localeMapper[defaultLocale])
            ?.get(key.removePrefix(componentPrefix))
            ?: throw IllegalStateException("Message data not found for command: $key")

    protected fun getCreateBuilder(
        messageData: MessageDataSerializer,
        substitutor: Substitutor = Placeholder.globalPlaceholder,
    ): MessageCreateBuilder {
        val builder = MessageCreateBuilder()

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

    private fun buildEmbeds(embeds: List<EmbedSetting>, substitutor: Substitutor? = null): List<MessageEmbed> =
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
}
