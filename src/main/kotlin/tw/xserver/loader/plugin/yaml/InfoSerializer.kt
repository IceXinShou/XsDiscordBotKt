package tw.xserver.loader.plugin.yaml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InfoSerializer(
    val author: String? = null,
    val main: String,
    val name: String,
    val description: String? = null,
    val version: String,
    val prefix: String = name,
    val depend: List<String> = emptyList(),


    @SerialName("soft_depend")
    val softDepend: List<String> = emptyList(),

    @SerialName("component_prefix")
    val componentPrefix: String = "",
)
