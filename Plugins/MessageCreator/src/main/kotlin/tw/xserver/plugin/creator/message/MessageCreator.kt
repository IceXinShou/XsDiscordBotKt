package tw.xserver.plugin.creator.message

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.plugin.Event
import tw.xserver.plugin.creator.message.setting.MessageData.EmbedSetting
import tw.xserver.plugin.placeholder.Substitutor

object MessageCreator : Event(true) {
    private val logger: Logger = LoggerFactory.getLogger(MessageCreator::class.java)

    override fun load() {
        logger.info("MessageCreator loaded")
    }

    override fun unload() {
        logger.info("MessageCreator unloaded")
    }

    fun buildEmbeds(embeds: List<EmbedSetting>, substitutor: Substitutor? = null): List<MessageEmbed> =
        embeds.mapNotNull { embed -> buildEmbed(embed, substitutor) }

    private fun buildEmbed(embed: EmbedSetting, substitutor: Substitutor?): MessageEmbed? {
        val builder = EmbedBuilder()

        // Set author once using substitutor if available or directly if not
        embed.author?.let { author ->
            builder.setAuthor(
                substitutor?.parse(author.name) ?: author.name,
                substitutor?.parse(author.url) ?: author.url,
                substitutor?.parse(author.iconUrl) ?: author.iconUrl
            )
        }

        // Handle title, description, thumbnail, and image with or without substitutor
        if (substitutor != null) {
            embed.title?.let { title -> builder.setTitle(substitutor.parse(title), substitutor.parse(embed.url)) }
            embed.description?.let { desc -> builder.setDescription(substitutor.parse(desc)) }
            embed.setThumbnail?.let { thumb -> builder.setThumbnail(substitutor.parse(thumb)) }
            embed.image?.let { image -> builder.setImage(substitutor.parse(image)) }
        } else {
            embed.title?.let { title -> builder.setTitle(title, embed.url) }
            embed.description?.let { desc -> builder.setDescription(desc) }
            embed.setThumbnail?.let { thumb -> builder.setThumbnail(thumb) }
            embed.image?.let { image -> builder.setImage(image) }
        }

        // Apply color and timestamp directly since they don't involve parsing
        embed.color?.let {
            builder.setColor(
                it.uppercase()
                    .removePrefix("0X")
                    .removePrefix("#")
                    .removeSuffix("H").toInt(radix = 16)
            )
        }
        embed.timestamp?.let { builder.setTimestamp(it) }

        // Set footer similarly to author
        embed.footer?.let { footer ->
            builder.setFooter(
                substitutor?.parse(footer.text) ?: footer.text,
                substitutor?.parse(footer.iconUrl) ?: footer.iconUrl
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
