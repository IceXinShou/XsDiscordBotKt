package tw.xserver.plugin.creator.message

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.apache.commons.lang3.StringUtils.isNumeric
import tw.xserver.loader.plugin.PluginEvent
import tw.xserver.plugin.creator.message.serializer.MessageDataSerializer.EmbedSetting
import tw.xserver.plugin.placeholder.Substitutor
import java.time.Instant
import java.time.OffsetDateTime

object Event : PluginEvent(true) {
    override fun load() {}

    override fun unload() {}
}
