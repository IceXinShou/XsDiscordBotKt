package tw.xserver.loader.base

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.base.MainLoader.globalCommands
import tw.xserver.loader.base.MainLoader.guildCommands
import tw.xserver.loader.base.MainLoader.listeners
import tw.xserver.loader.plugin.PluginEvent
import tw.xserver.loader.plugin.yaml.InfoSerializer
import java.io.File
import java.util.jar.JarFile

/**
 * Manages the lifecycle of all plugins, loading and unloading them as required.
 */
object PluginLoader {
    private val logger: Logger = LoggerFactory.getLogger(PluginLoader::class.java)
    private val dir: File = File("./plugins")
    private val plugins: MutableMap<String, InfoSimple> = HashMap()
    val pluginQueue = LinkedHashMap<String, PluginEvent>()

    /**
     * Loads all plugins from the plugins directory.
     */
    fun run() {
        var count = 0
        var fail = 0
        logger.info("Start loading plugins...")

        val loader = tw.xserver.loader.plugin.ClassLoader()
        dir.mkdirs()

        dir.listFiles()?.filter { it.extension == "jar" }?.forEach { file ->
            JarFile(file).use { jarFile ->
                try {
                    jarFile.getInputStream(jarFile.getEntry("info.yml")).use { inputStream ->
                        val config = Yaml().decodeFromStream<InfoSerializer>(inputStream)
                        logger.info("Loading {}...", config.name)

                        if (plugins.containsKey(config.name)) {
                            logger.error("Duplicate plugin name: ${file.name}")
                            fail++
                            return
                        }
                        loader.addJar(file, config.main)
                        val curPlugin =
                            loader.getClass(config.main)?.getDeclaredField("INSTANCE")?.get(null) as? PluginEvent
                        curPlugin?.let {
                            plugins[config.name] = InfoSimple(config.name, it, config.depend, config.soft_depend)
                            count++
                        } ?: run { fail++ }

                        logger.info("Loaded {}!", config.name)
                    }
                } catch (e: Exception) {
                    fail++
                    logger.error("Error occurred with file: ${file.name}", e)
                }
            }
        }

        plugins.values.forEach { info ->
            if (!pluginQueue.containsKey(info.name) && loadPlugin(info)) {
                logger.error("Stopped loading plugins due to missing dependencies.")
                return
            }
        }

        pluginQueue.values.forEach { plugin ->
            plugin.guildCommands()?.let { guildCommands.addAll(it) }
            plugin.globalCommands()?.let { globalCommands.addAll(it) }

            if (plugin.listener) listeners.add(plugin)
        }

        if (fail > 0) logger.error("$fail plugin(s) failed to load.")
        logger.info("{} plugin(s) loaded successfully.", count)
    }

    /**
     * Reloads all plugins by calling their reload methods.
     */
    fun reload() {
        pluginQueue.values.forEach { it.reloadAll() }
    }

    /**
     * Recursively loads a plugin and its dependencies.
     *
     * @param pluginInfo Information about the plugin to load.
     * @return True if there is a failure in dependency loading, false otherwise.
     */
    private fun loadPlugin(pluginInfo: InfoSimple?): Boolean {
        // Check if all mandatory dependencies are loaded successfully.
        pluginInfo?.depend?.forEach { depend ->
            // If the dependency is not already loaded, and it exists in the plugin list, try to load it.
            if (!pluginQueue.containsKey(depend)) {
                if (plugins.containsKey(depend)) {
                    // If loading the dependency fails, log and return true to indicate a failure.
                    if (loadPlugin(plugins[depend])) {
                        logger.error("Failed to load dependency $depend for plugin ${pluginInfo.name}.")
                        return true
                    }
                } else {
                    // If the dependency does not exist, log an error and return true.
                    logger.error("Plugin: ${pluginInfo.name} is missing dependency: $depend")
                    return true
                }
            }
        }

        // Check soft dependencies but do not fail if they are missing.
        pluginInfo?.softDepend?.forEach { depend ->
            if (!pluginQueue.containsKey(depend) && plugins.containsKey(depend)) {
                loadPlugin(plugins[depend])
            }
        }

        // All dependencies are satisfied, load the plugin.
        pluginInfo?.let {
            if (!pluginQueue.containsKey(it.name)) {
                pluginQueue[it.name] = it.pluginInstance
                logger.info("Initializing {}", it.name)
                it.pluginInstance.load()
                logger.info("{} load successfully", it.name)
            }
        }

        return false
    }
}

private class InfoSimple(
    val name: String,
    val pluginInstance: PluginEvent,
    val depend: List<String>,
    val softDepend: List<String>
)
