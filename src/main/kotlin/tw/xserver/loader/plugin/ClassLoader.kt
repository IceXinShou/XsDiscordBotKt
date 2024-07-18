package tw.xserver.loader.plugin

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.base.MainLoader
import java.io.File
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.FileSystems
import java.nio.file.Files

/**
 * A custom URLClassLoader to load classes and resources from JAR files dynamically.
 * This allows plugins or modules to be loaded into the application at runtime.
 */
class ClassLoader : URLClassLoader(arrayOfNulls(0), MainLoader::class.java.classLoader) {
    private val resourcePath: MutableMap<String, URL> = HashMap()

    /**
     * Adds a JAR file to the class loader's path. This method allows dynamic loading of JAR files
     * containing classes and resources.
     *
     * @param file The JAR file to be added.
     * @param main The main class path used to identify the JAR's resource path.
     * @throws RuntimeException if the resource path is already in use.
     */
    fun addJar(file: File, main: String) {
        val mainPath = main.substring(0, main.lastIndexOf('.')).replace('.', '/')
        try {
            val url = file.toURI().toURL()
            if (resourcePath.containsKey(mainPath)) {
                throw RuntimeException("Duplicate resource path: $mainPath")
            }
            resourcePath[mainPath] = url
            addURL(url)
        } catch (e: MalformedURLException) {
            logger.error("Adding jar file failed due to malformed URL: {}", e.message, e)
        }
    }

    /**
     * Loads a class by name.
     *
     * @param name The fully qualified name of the desired class.
     * @return The class object representing the desired class, or null if the class cannot be found.
     */
    fun getClass(name: String): Class<*>? {
        return try {
            loadClass(name, false)
        } catch (e: ClassNotFoundException) {
            logger.error("Class not found: {}", name)
            null
        }
    }

    /**
     * Gets the singleton instance of an object class by name.
     *
     * @param name The fully qualified name of the desired object class.
     * @return The singleton instance of the object class, or null if the class cannot be found or is not an object class.
     */
    fun getObjectInstance(name: String): Any? {
        val clazz = getClass(name)
        return if (clazz != null) {
            val field = clazz.getDeclaredField("INSTANCE")
            field.isAccessible = true
            field.get(null)
        } else {
            null
        }
//        } catch (e: ExceptionInInitializerError) {
//            logger.error(
//                "Initialization error when accessing instance of object class '{}': {}",
//                name,
//                e.cause?.message
//            )
//            null
//        } catch (e: Exception) {
//            logger.error("Error getting instance of object class '{}': {}", name, e.message)
//            null
//        }
    }

    /**
     * Overrides the standard URLClassLoader findResource method to add custom resource handling.
     * This method attempts to find a resource by name within the JAR files loaded by this class loader.
     *
     * @param name The resource name.
     * @return The URL of the resource, or null if the resource cannot be found.
     */
    override fun findResource(name: String): URL? {
        super.findResource(name)?.let { return it }

        val index = name.lastIndexOf('/')
        if (index == -1) return null

        resourcePath.forEach { (key, value) ->
            if (name.startsWith("$key/")) {
                try {
                    val uri = URI("jar:file:${value.path}!/${name.substring(key.length + 1)}")
                    FileSystems.newFileSystem(uri, emptyMap<String, Any>()).use { fileSystem ->
                        val path = fileSystem.getPath(name.substring(key.length + 1))
                        if (Files.exists(path)) {
                            return path.toUri().toURL()
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Error accessing resource: '$name'", e)
                }
            }
        }
        return null
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ClassLoader::class.java)
    }
}
