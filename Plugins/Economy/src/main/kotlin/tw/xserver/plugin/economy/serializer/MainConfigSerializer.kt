package tw.xserver.plugin.economy.serializer

import kotlinx.serialization.Serializable

@Serializable
data class MainConfigSerializer(
    val client_id: String,
    val client_secret: String,
    val port: Int,

    val sheet_id: String,
    val sheet_label: String,
    val sheet_range_id: String,
    val sheet_range_money: String,
    val sheet_range_cost: String,

    val admin_id: List<Long>,
    val board_user_show_limit: Int
)
