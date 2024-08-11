package tw.xserver.plugin.api.google.sheet.setting

import kotlinx.serialization.Serializable

@Serializable
data class AuthConfigSerializer (
    val client_id: String,
    val client_secret: String,
    val port: Int
)
