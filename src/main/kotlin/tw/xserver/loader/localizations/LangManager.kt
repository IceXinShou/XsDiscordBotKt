package tw.xserver.loader.localizations

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import net.dv8tion.jda.api.interactions.DiscordLocale
import okhttp3.internal.toImmutableMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.util.Arguments
import tw.xserver.loader.util.FileGetter
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * Manages language settings for Discord interaction localization.
 * @param D the type of localized data class that hold single localized language strings.
 * @param L the type of localized data class that holds all localized strings.
 * @param fileGetter used to access language files.
 * @param defaultLocale the default locale to be used when no locale-specific data is available.
 * @param clazzD the class type of the single localized data.
 * @param clazzL the class type of the all localized data.
 */
@OptIn(InternalSerializationApi::class)
class LangManager<D : Any, L : Any>(
    private val fileGetter: FileGetter,
    private val defaultLocale: DiscordLocale,
    private val clazzD: KClass<D>,
    private val clazzL: KClass<L>
) {
    private val lang: L = clazzL.objectInstance!!
    private val dir = File(fileGetter.dir, "./lang")

    init {
        dir.mkdirs()
        exportDefaultLang()

        var hasDefaultLocale = false
        val yaml = Yaml(serializersModule = SerializersModule {
            contextual(clazzD, clazzD.serializer())
        })

        dir.listFiles()?.filter { it.isDirectory }?.forEach { directory ->
            val registerFile = directory.resolve("register.yml")
            val locale = DiscordLocale.from(directory.name.replace("\\.\\w+$".toRegex(), ""))
            if (locale == DiscordLocale.UNKNOWN) {
                logger.warn("Cannot identify Discord locale from file: ${registerFile.canonicalPath}")
                return@forEach
            }

            if (locale == defaultLocale) hasDefaultLocale = true

            try {
                val decodedData: D = yaml.decodeFromString(clazzD.serializer(), registerFile.readText())
                val result = parseToMap(decodedData)
                result.forEach { (path, value) ->
                    getFieldByPath(lang, path).apply {
                        if (locale == defaultLocale) setDefaultLocale(locale)
                        set(locale, value)
                    }
                }

            } catch (e: Exception) {
                logger.error("Error decoding data for locale $locale from file: ${registerFile.name}", e)
            }
        }
        if (!hasDefaultLocale) {
            throw IllegalStateException("Default locale not found in the provided language files.")
        }
    }

    /**
     * Exports default language files from the resources to the language folder.
     * @throws IOException if default language files are not found or cannot be exported.
     */
    @Throws(IOException::class)
    private fun exportDefaultLang() {
        val langFilenames = fileGetter.getResourceFilenameList("./lang/")
        if (langFilenames.isEmpty()) {
            logger.error("No default language files found.")
            throw FileNotFoundException("Default language files not found.")
        }

        langFilenames.forEach { langFilename ->
            val langFile = File(dir, "./$langFilename")
            if (Arguments.forceExportResources || !langFile.exists()) {
                fileGetter.exportResource("./lang/$langFilename", langFile)
            }
        }
    }

    private fun parseToMap(obj: Any): Map<String, String> {
        val result = mutableMapOf<String, String>() // Only store String results
        fun exploreFields(obj: Any, parentPrefix: String) {
            obj::class.memberProperties.forEach { property ->
                val fieldName = if (parentPrefix.isEmpty()) property.name else "$parentPrefix.${property.name}"
                try {
                    val field = obj.javaClass.getDeclaredField(property.name)
                    field.isAccessible = true
                    val value = field.get(obj)
                    if (value is String) {
                        result[fieldName] = value
                    } else if (value != null) {
                        // Only explore further if the value is not a primitive or String (assumes custom types are complex)
                        if (!field.type.isPrimitive && field.type != String::class.java) {
                            exploreFields(value, fieldName)
                        }
                    }
                } catch (e: NoSuchFieldException) {
                    logger.error("Field not found: ${property.name}", e)
                } catch (e: IllegalAccessException) {
                    logger.error("Access to field denied: ${property.name}", e)
                }
            }
        }
        exploreFields(obj, "")
        return result.toImmutableMap()
    }

    private fun getFieldByPath(obj: Any, path: String): LocaleData {
        var currentObj: Any? = obj
        val pathSegments = path.split('.')
        try {
            for (segment in pathSegments) {
                val field: Field = try {
                    currentObj!!.javaClass.getDeclaredField(segment)
                } catch (e: NoSuchFieldException) {
                    currentObj!!.javaClass.getField(segment)
                }.apply { isAccessible = true }

                currentObj = field.get(currentObj)

                if (currentObj is LocaleData) {
                    return currentObj
                }
            }
        } catch (e: NoSuchFieldException) {
            logger.error("Field not found in the path: $path", e)
        } catch (e: IllegalAccessException) {
            logger.error("Access to field denied in the path: $path", e)
        } catch (e: Exception) {
            logger.error("Error processing path: $path", e)
        }

        throw NoSuchFieldException("LocaleData not found in the end of path: $path")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
