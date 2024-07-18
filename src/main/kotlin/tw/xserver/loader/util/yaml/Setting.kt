package tw.xserver.loader.util.yaml

import kotlinx.serialization.Serializable

@Serializable
data class Setting(
    val generalSettings: GeneralSettings
) {
    @Serializable
    data class GeneralSettings(
        val botToken: String,
        val activityMessage: List<String>
    )
}
