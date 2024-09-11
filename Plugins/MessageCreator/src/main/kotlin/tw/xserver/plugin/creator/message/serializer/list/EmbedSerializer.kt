package tw.xserver.plugin.creator.message.serializer.list

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.awt.Color

object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeString(
            String.format("#%06X", value.rgb and 0xFFFFFF)
        )
    }

    override fun deserialize(decoder: Decoder): Color {
        return Color.decode(decoder.decodeString())
    }
}