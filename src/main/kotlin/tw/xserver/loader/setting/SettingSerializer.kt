package tw.xserver.loader.setting

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SettingSerializer(
    @SerialName("general_settings")
    val generalSettings: GeneralSettings,

    @SerialName("builtin_settings")
    val builtinSettings: BuiltinSettings? = null,
) {
    @Serializable
    data class GeneralSettings(
        @SerialName("bot_token")
        val botToken: String, // required
    )

    @Serializable
    data class BuiltinSettings(
        @SerialName("status_changer_settings")
        val statusChangerSetting: StatusChangerSetting = StatusChangerSetting(),

        @SerialName("console_logger_settings")
        val consoleLoggerSetting: List<ConsoleLoggerSetting> = emptyList(),
    ) {
        @Serializable
        data class StatusChangerSetting(
            @SerialName("activity_messages")
            val activityMessages: List<String> = emptyList(),
        )

        @Serializable
        data class ConsoleLoggerSetting(
            @SerialName("guild_id")
            val guildId: Long,

            @SerialName("channel_id")
            val channelId: Long,

            @SerialName("log_type")
            val logType: List<String>,
            val format: String,
        )
    }
}
