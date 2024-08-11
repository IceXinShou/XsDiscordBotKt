package tw.xserver.loader.util.yaml

import kotlinx.serialization.Serializable

@Serializable
data class SettingSerializer(
    val generalSettings: GeneralSettings
) {
    @Serializable
    data class GeneralSettings(
        val botToken: String,
        val activityMessage: List<String>
    )
}
