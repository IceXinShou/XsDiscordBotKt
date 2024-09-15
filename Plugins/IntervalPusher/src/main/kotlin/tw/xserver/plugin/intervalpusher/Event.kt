package tw.xserver.plugin.intervalpusher

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import net.dv8tion.jda.api.events.session.ReadyEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.plugin.PluginEvent
import tw.xserver.loader.util.FileGetter
import tw.xserver.plugin.intervalpusher.serializer.MainConfigSerializer
import java.io.File
import java.io.IOException

object Event : PluginEvent(true) {
    private val PLUGIN_DIR_FILE = File("./plugins/IntervalPusher/")
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    private lateinit var config: MainConfigSerializer
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val pushers = mutableListOf<IntervalPusher>()


    override fun load() {
        reloadAll()
    }

    override fun unload() {
        pushers.forEach { it.stop() }
        coroutineScope.cancel()
    }

    override fun reloadConfigFile() {
        fileGetter = FileGetter(PLUGIN_DIR_FILE, this.javaClass)

        try {
            fileGetter.readInputStream("./config.yml").use {
                config = Yaml().decodeFromStream<MainConfigSerializer>(it)
            }
        } catch (e: IOException) {
            logger.error("Please configure ${PLUGIN_DIR_FILE.canonicalPath}./config.yml", e)
        }

        logger.info("Setting file loaded successfully")
    }

    override fun onReady(event: ReadyEvent) {
        for (listener in config.listeners) {
            val pusher = IntervalPusher(listener.url, listener.interval, coroutineScope)
            pusher.start()
            pushers.add(pusher)
        }
    }
}
