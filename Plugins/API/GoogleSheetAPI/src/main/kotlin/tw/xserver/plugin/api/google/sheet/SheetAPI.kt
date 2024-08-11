package tw.xserver.plugin.api.google.sheet

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.plugin.PluginEvent


object SheetAPI : PluginEvent(false) {
    private val logger: Logger = LoggerFactory.getLogger(SheetAPI::class.java)

    override fun load() {
        logger.info("Loaded SheetAPI")
    }

    override fun unload() {
        logger.info("UnLoaded SheetAPI")
    }
}