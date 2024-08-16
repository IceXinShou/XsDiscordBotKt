package tw.xserver.plugin.creator.message

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.apache.commons.lang3.StringUtils.isNumeric
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.plugin.PluginEvent
import tw.xserver.plugin.creator.message.setting.MessageDataSerializer.EmbedSetting
import tw.xserver.plugin.placeholder.Substitutor
import java.time.Instant
import java.time.OffsetDateTime

object MessageCreator : PluginEvent(true) {
    private val logger: Logger = LoggerFactory.getLogger(MessageCreator::class.java)

    override fun load() {}

    override fun unload() {}

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
            embed.title?.let { title -> builder.setTitle(substitutor.parse(title.text), substitutor.parse(title.url)) }
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
        embed.colorCode.let {
            builder.setColor(
                it.lowercase()
                    .removePrefix("0x") // 0xFFFFFF
                    .removePrefix("#")  // #FFFFFF
                    .removeSuffix("h")  // FFFFFh
                    .toInt(radix = 16)
            )
        }

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
