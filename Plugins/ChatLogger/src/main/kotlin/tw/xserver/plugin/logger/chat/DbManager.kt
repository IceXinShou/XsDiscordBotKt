package tw.xserver.plugin.logger.chat

import net.dv8tion.jda.api.entities.Guild
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.base.MainLoader.jdaBot
import tw.xserver.plugin.api.sqlite.SQLiteFileManager
import tw.xserver.plugin.logger.chat.ChatLogger.KEEP_ALL_LOG
import tw.xserver.plugin.logger.chat.Event.PLUGIN_DIR_FILE
import tw.xserver.plugin.logger.chat.JsonManager.dataMap
import java.io.File
import java.sql.Connection


internal object DbManager {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    // Long may be too small but for efficiency performance. Watchful waiting
    private val channelTableCache: MutableSet<Long> = mutableSetOf() // Avoid multiple database exist query

    @Synchronized
    internal fun initAfterReady() {
        val dataFolder = File(PLUGIN_DIR_FILE, "data")
        if (dataFolder.mkdirs()) {
            logger.info("Default data folder created")
        }

        dataFolder.listFiles()?.filter { it.isFile && it.extension == "db" }?.forEach fileLoop@{ file ->
            val guild: Guild? = jdaBot.getGuildById(file.nameWithoutExtension)
            if (!KEEP_ALL_LOG && guild == null) {
                file.delete()
                return@fileLoop
            }

            try {
                val conn = getConnection(file.nameWithoutExtension)
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT name FROM sqlite_master WHERE type ='table' AND name NOT LIKE 'sqlite_%';")
                        .use { rs ->
                            val notExistsChannelId: MutableList<Long> = mutableListOf()

                            while (rs.next()) {
                                val channelId = rs.getString("name").toLong()
                                if (jdaBot.getGuildChannelById(channelId) == null && !KEEP_ALL_LOG) {
                                    notExistsChannelId.add(channelId)
                                } else {
                                    channelTableCache.add(channelId)
                                }
                            }

                            // Drop tables that no longer have a corresponding channel
                            if (notExistsChannelId.isNotEmpty()) {
                                conn.createStatement().use { stmt ->
                                    notExistsChannelId.forEach { tableName ->
                                        stmt.executeUpdate("DROP TABLE IF EXISTS $tableName;")
                                    }
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                logger.error("Error processing database file: ${file.name}", e)
            }
        }
    }

    internal fun receiveMessage(guildId: String, channelId: Long, messageId: Long, userId: Long, msg: String) {
        val conn = getConnection(guildId)
        initChannelTable(conn, channelId)

        val insert = "INSERT INTO '${channelId}' VALUES (?, ?, ?, ?)"

        conn.prepareStatement(insert).use {
            it.setLong(1, messageId)
            it.setLong(2, userId)
            it.setString(3, msg)
            it.setInt(4, 0)
            it.executeUpdate()
        }
    }

    internal fun updateMessage(guildId: String, channelId: Long, messageId: Long, newMsg: String): QueriedMessageData {
        val conn = getConnection(guildId)
        initChannelTable(conn, channelId)

        return queryMessage(conn, channelId, messageId).let { queryData ->
            queryData.updateCounter()

            conn.prepareStatement("UPDATE '$channelId' SET message = ?, update_count = ? WHERE message_id = ?").use {
                it.setString(1, newMsg)
                it.setInt(2, queryData.updateCount)
                it.setLong(3, messageId)
                it.executeUpdate()
            }

            queryData
        }
    }

    internal fun deleteMessage(
        guildId: String,
        channelId: Long,
        messageId: Long
    ): QueriedMessageData {
        val conn = getConnection(guildId)
        initChannelTable(conn, channelId)

        return queryMessage(conn, channelId, messageId)
    }

    internal fun deleteDatabase(guildId: String) {
        SQLiteFileManager.deleteDatabase(guildId)
    }

    /**
     * Checks if a channel is listenable based on the channel ID.
     * If the channel is not listenable and logging is not required for all, returns -1.
     *
     * @param channelId The ID of the channel to check.
     * @return The listenable status of the channel or -1 if not listenable and KEEP_ALL_LOG is false.
     */
    internal fun isListenable(channelId: Long): Boolean {
        return KEEP_ALL_LOG || channelId in channelTableCache
    }

    internal fun markChannelAsUnavailable(channelId: Long) {
        channelTableCache.remove(channelId)
    }

    internal data class QueriedMessageData(
        val message: String,
        val userId: Long,
        var updateCount: Int,
    ) {
        internal fun updateCounter() = updateCount++
    }

    internal fun disconnect() {
        dataMap.keys.forEach { channelId ->
            SQLiteFileManager.disconnect("CL:$channelId")
        }
    }

    /**
     * Throw MessageNotFound
     */
    private fun queryMessage(conn: Connection, channelId: Long, messageId: Long): QueriedMessageData {
        conn.createStatement().use {
            it.executeQuery(
                "SELECT message, user_id, update_count FROM '$channelId' WHERE message_id = '$messageId'"
            ).use { rs ->
                try {
                    return QueriedMessageData(
                        rs.getString("message"),
                        rs.getLong("user_id"),
                        rs.getInt("update_count")
                    )
                } catch (e: NullPointerException) {
                    throw MessageNotFound()
                }
            }
        }
    }

    private fun getConnection(guildId: String): Connection =
        SQLiteFileManager.getConnection("CL:$guildId")
            ?: SQLiteFileManager.addConnection("CL:$guildId", File(PLUGIN_DIR_FILE, "data/$guildId.db"))

    private fun initChannelTable(
        conn: Connection,
        detectChannelId: Long
    ) {
        // avoid multiple init
        if (detectChannelId in channelTableCache) return

        conn.createStatement().use { stmt ->
            stmt.executeUpdate(
                "CREATE TABLE '$detectChannelId' (" +
                        "message_id INT PRIMARY KEY  NOT NULL ON CONFLICT FAIL, " +
                        "user_id                INT  NOT NULL, " +
                        "message               TEXT  NOT NULL,  " +
                        "update_count           INT  NOT NULL  " +
                        ")",

                )
            channelTableCache.add(detectChannelId)
        }
    }
}
