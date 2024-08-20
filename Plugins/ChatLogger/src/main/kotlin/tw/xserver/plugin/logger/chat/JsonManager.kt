package tw.xserver.plugin.logger.chat

import com.google.gson.JsonObject
import net.dv8tion.jda.api.entities.Guild
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.base.MainLoader.jdaBot
import tw.xserver.loader.util.json.JsonObjFileManager
import tw.xserver.plugin.logger.chat.Event.DIR_PATH
import java.io.File

object JsonManager {
    private val channelSettingsMap: MutableMap<Long, MutableMap<Long, ChannelSetting>> = HashMap()
    private val fileManager: MutableMap<Long, JsonObjFileManager> = HashMap()
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun init() {
        channelSettingsMap.clear()
        fileManager.clear()

        val settingFolder = File(DIR_PATH, "setting")
        if (settingFolder.mkdirs()) {
            logger.info("Default setting folder created")
        }

        // delete unable access guild json files
        settingFolder.listFiles()?.forEach { file: File ->
            val guildId = file.nameWithoutExtension.toLong()
            val guild: Guild? = jdaBot.getGuildById(guildId)
            if (guild == null) {
                file.delete()
                return@forEach
            }

            val manager = JsonObjFileManager(file.canonicalFile)
            fileManager[guildId] = manager

            // put data from json files to channelSettings map
            manager.get().keySet().forEach { channelId: String ->
                // if channel cannot access, remove and skip it
                if (guild.getGuildChannelById(channelId) == null) {
                    manager.remove(channelId)
                } else {
                    val settingObj = manager.getAsJsonObject(channelId)

                    channelSettingsMap[guildId] = HashMap<Long, ChannelSetting>().apply {
                        set(

                            channelId.toLong(), ChannelSetting(
                                if (settingObj["allow_mode"].asBoolean)
                                    ChannelSetting.ChannelMode.Allow else ChannelSetting.ChannelMode.Block,
                                settingObj["allow"].asJsonObject,
                                settingObj["block"].asJsonObject
                            )
                        )
                    }
                }
            }
            manager.save()
        }
    }


    fun toggle(guildId: Long, channelId: Long): ChannelSetting {
        // update map
        val setting = channelSettingsMap
            .computeIfAbsent(guildId) { HashMap() }
            .computeIfAbsent(channelId) { ChannelSetting() }.toggle()

        // update json file
        val manager = fileManager[guildId]!!
        val obj = manager.getAsJsonObject(channelId.toString())

        obj.addProperty("allow_mode", setting.channelMode == ChannelSetting.ChannelMode.Allow)
        manager.save()

        return setting
    }

    fun addChannels(
        guildId: Long,
        rootId: Long,
        channelIds: List<Long>,
        channelMode: ChannelSetting.ChannelMode
    ): ChannelSetting {
        val manager = fileManager[guildId]!!
        val obj = manager.getAsJsonObject(rootId.toString())

        val channelsObj =
            obj[if (channelMode == ChannelSetting.ChannelMode.Allow) "allow" else "block"].asJsonObject
        for (channelId in channelIds) {
            channelsObj.add(channelId.toString(), JsonObject().apply {
                addProperty("update", true)
                addProperty("delete", true)
            })
        }

        manager.save()
        return channelSettingsMap
            .computeIfAbsent(guildId) { HashMap() }
            .computeIfAbsent(rootId) { ChannelSetting() }
            .add(channelsObj, channelMode)
    }

    fun delete(guildId: Long, channelId: Long) {
        channelSettingsMap[guildId]!!.remove(channelId)
        fileManager[guildId]!!.remove(channelId.toString()).save()
    }

    fun getOrDefault(guildId: Long, channelId: Long): ChannelSetting {
        val settingMap = channelSettingsMap.computeIfAbsent(guildId) { HashMap() }
        settingMap[channelId]?.let { return it }

        val manager = JsonObjFileManager(File(DIR_PATH, "setting/$guildId.json"))
        fileManager[guildId] = manager

        val setting = ChannelSetting()
        settingMap[channelId] = setting

        manager.add(channelId.toString(), JsonObject().apply {
            addProperty("allow_mode", true)
            add("allow", JsonObject())
            add("block", JsonObject())
        })
        manager.save()

        return setting
    }
}