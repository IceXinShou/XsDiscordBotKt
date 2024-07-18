package tw.xserver.plugin.placeholder

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.plugin.Event

object Placeholder : Event(true) {
    private val logger: Logger = LoggerFactory.getLogger(Placeholder::class.java)

    override fun load() {
        logger.info("Loaded Placeholder")
    }

    override fun unload() {
        logger.info("UnLoaded Placeholder")
    }
}
