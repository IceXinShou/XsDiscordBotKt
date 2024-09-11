package tw.xserver.plugin.logger.chat

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import tw.xserver.loader.base.MainLoader.jdaBot

internal class ChannelData(
    private val guild: Long,
    initData: JsonObject? = null
) {
    private var channelMode: ChannelMode = ChannelMode.Allow
    private val allow: MutableSet<Long> = HashSet()
    private val block: MutableSet<Long> = HashSet()

    fun getJsonObject(): JsonObject = JsonObject().apply {
        addProperty("allow_mode", getChannelMode())
        add("allow", getAllowArray())
        add("block", getBlockArray())
    }

    fun getChannelMode(): Boolean = channelMode == ChannelMode.Allow
    fun getAllowArray(): JsonArray = JsonArray().apply { allow.forEach(::add) }
    fun getBlockArray(): JsonArray = JsonArray().apply { block.forEach(::add) }

    init {
        initData?.let {
            when (it["allow_mode"].asBoolean) {
                true -> ChannelMode.Allow
                false -> ChannelMode.Block
            }.also { mode -> channelMode = mode }

            addAll(it)
        }
    }

    fun getCurrentDetectChannels(): List<Long> = when (channelMode) {
        ChannelMode.Allow -> {
            allow.toList()
        }

        ChannelMode.Block -> {
            jdaBot.getGuildById(guild)!!.channels
                .map { it.idLong }
                .filter { it !in block }
        }
    }

    fun toggle(): ChannelData {
        channelMode = if (channelMode == ChannelMode.Allow) {
            ChannelMode.Block
        } else {
            ChannelMode.Allow
        }
        return this
    }

    fun addAllows(detectedChannelIds: List<Long>): ChannelData {
        detectedChannelIds.forEach(allow::add)
        return this
    }

    fun addBlocks(detectedChannelIds: List<Long>): ChannelData {
        detectedChannelIds.forEach(block::add)
        return this
    }

    fun addAll(obj: JsonObject): ChannelData {
        addAllows(obj["allow"].asJsonArray.map { it.asLong })
        addBlocks(obj["block"].asJsonArray.map { it.asLong })
        return this
    }

    enum class ChannelMode {
        Allow,
        Block
    }
}