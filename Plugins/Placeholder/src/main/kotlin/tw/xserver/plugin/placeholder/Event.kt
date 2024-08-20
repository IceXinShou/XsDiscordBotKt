package tw.xserver.plugin.placeholder

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.plugin.PluginEvent

object Event : PluginEvent(true) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun load() {}

    override fun unload() {}
}
