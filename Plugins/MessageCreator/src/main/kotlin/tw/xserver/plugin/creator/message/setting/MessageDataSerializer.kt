package tw.xserver.plugin.creator.message.setting

import kotlinx.serialization.Serializable
import java.time.temporal.TemporalAccessor

@Serializable
data class MessageDataSerializer(
    val content: String = "",
    val embeds: List<EmbedSetting> = emptyList(),
) {
    @Serializable
    data class EmbedSetting(
        var author: AuthorSetting? = null,
        var title: String? = null,
        var description: String? = null,
        var url: String? = null,
        var setThumbnail: String? = null,
        var image: String? = null,
        var color: String? = null,
        var footer: FooterSetting? = null,
        val timestamp: TemporalAccessor? = null, // no placeholder support, TODO: fix
        var fields: List<FieldSetting> = emptyList(),
    ) {
        @Serializable
        data class AuthorSetting(
            var name: String,
            var url: String? = null,
            var iconUrl: String? = null,
        )

        @Serializable
        data class FooterSetting(
            var text: String,
            var iconUrl: String? = null,
        )

        @Serializable
        data class FieldSetting(
            var name: String,
            var value: String,
            var inline: Boolean, // no placeholder support
        )
    }
}
