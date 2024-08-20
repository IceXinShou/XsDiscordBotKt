package tw.xserver.loader.util.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color

object ColorSerializer : KSerializer<Color> {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    private val colorMap: Map<String, Color> = mapOf(
        "white" to Color.white,
        "black" to Color.black,

        "lightGray" to Color.lightGray,
        "gray" to Color.gray,
        "darkGray" to Color.darkGray,


        "red" to Color.red,
        "green" to Color.green,
        "blue" to Color.blue,

        "yellow" to Color.yellow,
        "magenta" to Color.magenta,
        "cyan" to Color.cyan,

        "pink" to Color.pink,
        "orange" to Color.orange,
    )

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        val hexRed = value.red.toString(16).padStart(2, '0')
        val hexGreen = value.green.toString(16).padStart(2, '0')
        val hexBlue = value.blue.toString(16).padStart(2, '0')
        encoder.encodeString("#$hexRed$hexGreen$hexBlue".uppercase())
    }

    override fun deserialize(decoder: Decoder): Color {
        val orgStr = decoder.decodeString()
        val str = orgStr.lowercase()
        return if (
            ((str.startsWith("#") || str.endsWith("h")) && str.length == 7) ||
            (str.startsWith("0x") && str.length == 8)
        ) {
            Color(
                str
                    .removePrefix("#")
                    .removePrefix("0x")
                    .removeSuffix("h")
                    .toInt(16)
            )
        } else {
            colorMap.getOrElse(str) {
                logger.warn("Unsupported color: $orgStr")
                Color.BLACK
            }
        }
    }
}