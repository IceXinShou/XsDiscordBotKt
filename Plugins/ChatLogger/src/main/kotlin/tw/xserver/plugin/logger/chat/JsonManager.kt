package tw.xserver.plugin.logger.chat

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.dv8tion.jda.api.entities.Guild
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.base.MainLoader.jdaBot
import tw.xserver.loader.json.JsonObjFileManager
import tw.xserver.plugin.logger.chat.Event.PLUGIN_DIR_FILE
import java.io.File

internal object JsonManager {
    private val fileManagers: MutableMap<Long, JsonObjFileManager> = HashMap()
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    // listen map to setting
    internal val dataMap: MutableMap<Long, ChannelData> = HashMap()

    fun initAfterReady() {
        val settingFolder = File(PLUGIN_DIR_FILE, "setting")
        if (settingFolder.mkdirs()) {
            logger.info("Default setting folder created")
        }

        // delete unable access guild json files
        settingFolder.listFiles()?.filter { it.isFile && it.extension == "json" }?.forEach fileLoop@{ file: File ->
            val guildId = file.nameWithoutExtension.toLong()
            val guild: Guild? = jdaBot.getGuildById(guildId)
            if (guild == null) {
                file.delete()
                return@fileLoop
            }

            val fileManager = JsonObjFileManager(file)
            this.fileManagers[guildId] = fileManager

            // put data from json files to channelSettings map
            fileManager.get().entrySet().forEach channelLoop@{ (listenChannelId, detectChannelObj) ->
                if (guild.getGuildChannelById(listenChannelId) == null) {
                    fileManager.remove(listenChannelId)
                    return@channelLoop
                }

                if (detectChannelObj !is JsonObject) return@channelLoop

                fileManager.getAsJsonObject(listenChannelId).apply {
                    add("allow", JsonArray().apply {
                        detectChannelObj["allow"].asJsonArray
                            .filter { guild.getGuildChannelById(it.asLong) != null }
                            .map { it.asLong }
                            .forEach { add(it) }
                    })
                    add("block", JsonArray().apply {
                        detectChannelObj["block"].asJsonArray
                            .filter { guild.getGuildChannelById(it.asLong) != null }
                            .map { it.asLong }
                            .forEach { add(it) }
                    })

                    dataMap[listenChannelId.toLong()] = ChannelData(guildId, this)
                }
            }

            fileManager.save()
        }
    }

    private fun getOrPut(guildId: Long, listenChannelId: Long): Pair<JsonObjFileManager, JsonObject> {
        val fileManager = fileManagers
            .getOrPut(guildId) { JsonObjFileManager(File(PLUGIN_DIR_FILE, "setting/$guildId.json")) }
        val obj: JsonObject =
            fileManager.computeIfAbsent(listenChannelId.toString(), ChannelData(guildId).getJsonObject()).asJsonObject

        return Pair(fileManager, obj)
    }

    internal fun toggle(guildId: Long, listenChannelId: Long): ChannelData {
        // update map
        val setting = getChannelData(listenChannelId, guildId).toggle()

        // update json file
        val (fileManager, obj) = getOrPut(guildId, listenChannelId)
        obj.addProperty("allow_mode", setting.getChannelMode())
        fileManager.save()

        return setting
    }

    internal fun addAllowChannels(
        guildId: Long,
        listenChannelId: Long,
        detectedChannelIds: List<Long>,
    ): ChannelData {
        // update map
        val setting = getChannelData(listenChannelId, guildId).addAllows(detectedChannelIds)

        // update json file
        val (fileManager, obj) = getOrPut(guildId, listenChannelId)
        obj.add("allow", setting.getAllowArray())
        fileManager.save()

        return setting
    }

    internal fun addBlockChannels(
        guildId: Long,
        listenChannelId: Long,
        detectedChannelIds: List<Long>,
    ): ChannelData {
        // update map
        val setting = getChannelData(listenChannelId, guildId).addBlocks(detectedChannelIds)

        // update json file
        val (fileManager, obj) = getOrPut(guildId, listenChannelId)
        obj.add("block", setting.getBlockArray())
        fileManager.save()

        return setting
    }

    fun delete(guildId: Long, channelId: Long) {
        // remove map
        dataMap.remove(channelId)

        // remove json file
        fileManagers[guildId]?.remove(channelId.toString())?.save()
    }

    // getOrDefault
    private fun getChannelData(listenChannelId: Long, guildId: Long): ChannelData {
        return dataMap.computeIfAbsent(listenChannelId) { ChannelData(guildId) }
    }
}