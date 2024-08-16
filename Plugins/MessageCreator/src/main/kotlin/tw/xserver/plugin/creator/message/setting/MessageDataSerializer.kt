package tw.xserver.plugin.creator.message.setting

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDataSerializer(
    val content: String = "", // 2000 length limit
    val embeds: List<EmbedSetting> = emptyList(), // 10 size limit
    val components: List<Component> = emptyList(), // 5 size limit, allowed format: [ "buttons", "string_select_menu", "entity_select_menu" ]
) {
    @Serializable
    data class EmbedSetting(
        var author: AuthorSetting? = null,
        var title: TitleSetting? = null,
        var description: String? = null, // 4096 length limit

        @SerialName("thumbnail_url")
        var thumbnailUrl: String? = null, // 2000 length limit

        @SerialName("image_url")
        var imageUrl: String? = null, // 2000 length limit

        @SerialName("color_code")
        var colorCode: String = "#FFFFFF", // default: "#FFFFFF", allowed format: [ "0xFFFFFF", "#FFFFFF", "FFFFFFh" ]
        var footer: FooterSetting? = null,
        val timestamp: String? = null, // allowed format: [ "%now%", "1723675788491" ]
        var fields: List<FieldSetting> = emptyList(), // 25 size limit
    ) {
        @Serializable
        data class AuthorSetting(
            var name: String, // 256 length limit
            var url: String? = null, // 2000 length limit

            @SerialName("icon_url")
            var iconUrl: String? = null, // 2000 length limit
        )

        @Serializable
        data class TitleSetting(
            var text: String, // 256 length limit
            var url: String? = null, // 2000 length limit
        )

        @Serializable
        data class FooterSetting(
            var text: String, // 2048 length limit

            @SerialName("icon_url")
            var iconUrl: String? = null, // 2000 length limit
        )

        @Serializable
        data class FieldSetting(
            var name: String, // 256 length limit
            var value: String, // 1024 length limit
            var inline: Boolean = false,
        )
    }


    @Serializable
    sealed class Component {
        @Serializable
        @SerialName("buttons")
        data class ButtonsComponent(
            val buttons: List<Button>
        ) : Component() {
            @Serializable
            data class Button(
                @SerialName("uid_or_url")
                val uidOrUrl: String,
                val style: Int,
                val label: String? = null,
                val disabled: Boolean = false,
                val emoji: Emoji? = null
            ) {
                @Serializable
                data class Emoji(
                    val name: String,
                    val animated: Boolean = false
                )
            }
        }

        @Serializable
        @SerialName("string_select_menu")
        data class StringSelectMenuSetting(
            val uid: String,
            val placeholder: String? = null,
            val min: Int = 1,
            val max: Int = 1,
            val options: List<Option>
        ) : Component() {
            @Serializable
            data class Option(
                val label: String,
                val value: String,
                val description: String? = null,
                val default: Boolean = false,
                val emoji: Emoji? = null
            ) {
                @Serializable
                data class Emoji(
                    val name: String,
                    val animated: Boolean = false
                )
            }
        }

        @Serializable
        @SerialName("entity_select_menu")
        data class EntitySelectMenuSetting(
            val uid: String,
            val placeholder: String? = null,
            val min: Int = 1,
            val max: Int = 1,

            @SerialName("select_target_type")
            val selectTargetType: String,

            @SerialName("channel_types")
            val channelTypes: List<String> = emptyList(),
        ) : Component()
    }
}
