package tw.xserver.loader.plugin.yaml

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val author: String? = null,
    val main: String,
    val name: String,
    val description: String? = null,
    val version: String,
    val prefix: String? = null,
    val depend: List<String> = emptyList(),
    val soft_depend: List<String> = emptyList(),
)
