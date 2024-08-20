package tw.xserver.plugin.api.sqlite

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import javax.management.openmbean.KeyAlreadyExistsException

class SQLiteFileManager {
    private val dbConn: MutableMap<Long, Connection> = HashMap()

    fun getConnection(uniqueKey: Long): Connection = dbConn[uniqueKey]!!

    fun disconnect(uniqueKey: Long) {
        dbConn[uniqueKey]?.let {
            it.close()
            dbConn.remove(uniqueKey)
        } ?: throw NoSuchElementException("No connection associated with key: $uniqueKey")
    }

    fun addFileConnection(uniqueKey: Long, file: File): Connection {
        return addFileConnection(uniqueKey, file, false)
    }

    fun getOrDefaultConnection(uniqueKey: Long, file: File): Connection {
        if (!dbConn.containsKey(uniqueKey))
            return addFileConnection(uniqueKey, file, true)

        return getConnection(uniqueKey)
    }

    @Synchronized
    private fun addFileConnection(uniqueKey: Long, file: File, skipCheck: Boolean): Connection {
        if (!skipCheck && dbConn.containsKey(uniqueKey))
            throw KeyAlreadyExistsException("Connection key $uniqueKey already exists.")

        val connection = DriverManager.getConnection("jdbc:sqlite:${file.canonicalPath}")
        dbConn[uniqueKey] = connection

        return connection
    }
}
