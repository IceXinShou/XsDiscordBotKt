package tw.xserver.plugin.logger.chat

import com.google.gson.JsonObject

class ChannelSetting(
    var channelMode: ChannelMode = ChannelMode.Allow,
    allow: JsonObject? = null,
    block: JsonObject? = null
) {
    val allow: MutableSet<ListData> = HashSet()
    val block: MutableSet<ListData> = HashSet()

    init {
        allow?.let { add(it, ChannelMode.Allow) }
        block?.let { add(it, ChannelMode.Block) }
    }

    fun toggle(): ChannelSetting {
        channelMode = if (channelMode == ChannelMode.Allow) {
            ChannelMode.Block
        } else {
            ChannelMode.Allow
        }
        return this
    }

    fun add(obj: JsonObject, channelMode: ChannelMode): ChannelSetting {
        for (detectId in obj.keySet()) {
            val setting = obj[detectId].asJsonObject
            val data = ListData(
                detectId.toLong(),
                setting["update"].asBoolean,
                setting["delete"].asBoolean
            )

            if (channelMode == ChannelMode.Allow) allow.add(data)
            else block.add(data)
        }
        return this
    }

    fun contains(channelId: Long, type: DetectType): Boolean {
        return if (channelMode == ChannelMode.Allow) {
            allow.stream().anyMatch { i: ListData -> i.contains(channelId, type) }
        } else {
            block.stream().noneMatch { i: ListData -> i.contains(channelId, type) }
        }
    }

    class ListData(val detectId: Long, private val update: Boolean, private val delete: Boolean) {
        fun contains(channelId: Long, type: DetectType): Boolean {
            if (detectId == channelId) {
                return ((update && type == DetectType.UPDATE) || (delete && type == DetectType.DELETE))
            }

            return false
        }

        override fun equals(other: Any?): Boolean {
            if (other is ListData) return (detectId == other.detectId)

            return false
        }

        override fun hashCode(): Int {
            return detectId.toString().hashCode()
        }
    }

    enum class DetectType {
        UPDATE,
        DELETE
    }

    enum class ChannelMode {
        Allow,
        Block
    }
}