package tw.xserver.plugin.creator.message.serializer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle

@Serializable
data class ModalDataSerializer(
    @SerialName("uid")
    val customId: String,

    val title: String,

    @SerialName("text_inputs")
    val textInputs: List<TextInputSetting> = emptyList(),
) {
    @Serializable
    data class TextInputSetting(
        val uid: String,
        val label: String,
        val value: String? = null,
        val placeholder: String? = null,

        @SerialName("min_length")
        val minLength: Int = -1,

        @SerialName("max_length")
        val maxLength: Int = -1,

        val style: TextInputStyle,
        val required: Boolean = true,
    )
}
