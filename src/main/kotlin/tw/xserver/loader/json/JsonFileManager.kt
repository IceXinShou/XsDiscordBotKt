package tw.xserver.loader.json

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

abstract class JsonFileManager<T : JsonElement>(
    private val file: File,
    private val dataType: Class<T>
) : AutoCloseable {
    protected lateinit var data: T
    protected abstract fun defaultFileAndData(): T

    init {
        initData()
    }

    @Synchronized
    private fun initData() {
        this.use {
            if (file.exists()) {
                val fileText = file.readText()
                if (fileText.isNotEmpty()) {
                    try {
                        data = Gson().fromJson(fileText, dataType)
                    } catch (e: JsonSyntaxException) {
                        logger.error("Bad format for file: ${file.name}", e)
                        return
                    }
                    return
                }
            }

            data = defaultFileAndData()
        }
    }

    @Synchronized
    fun save() = try {
        file.writeText(data.toString())
    } catch (e: IOException) {
        logger.error("Cannot save file.", e)
    }

    override fun close() {
        save()
    }

    companion object {
        protected val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}