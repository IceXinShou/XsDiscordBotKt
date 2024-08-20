package tw.xserver.plugin.economy.storage

import com.google.gson.JsonObject
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import tw.xserver.loader.util.GlobalUtil.getUserById
import tw.xserver.loader.util.json.JsonObjFileManager
import tw.xserver.plugin.creator.message.serializer.MessageDataSerializer
import tw.xserver.plugin.economy.Economy.Type
import tw.xserver.plugin.economy.Event.config
import tw.xserver.plugin.economy.UserData
import tw.xserver.plugin.placeholder.Substitutor
import kotlin.math.min

/**
 * Manages user data and rankings via a JSON file system.
 */
internal object JsonManager {
    internal lateinit var json: JsonObjFileManager
    private val userData: MutableMap<Long, UserData> = HashMap()
    private val moneyBoard: MutableList<UserData> = ArrayList()
    private val costBoard: MutableList<UserData> = ArrayList()

    /**
     * Initializes the JSON file by loading existing users or creating new entries.
     */
    fun initFile() {
        json.get().keySet().forEach { key ->
            val id = key.toLong()
            val obj = json.computeIfAbsent(key, JsonObject()).getAsJsonObject()
            try {
                getUserById(id).apply {
                    userData[id] = UserData(id, obj["money"].asInt, obj["cost"].asInt, name)
                }
            } catch (e: ErrorResponseException) {
                UserData(id, obj["money"].asInt, obj["cost"].asInt, "unknown ($key)").also {
                    userData[id] = it
                }
            }
        }

        moneyBoard.addAll(userData.values)
        costBoard.addAll(userData.values)
        sortMoneyBoard()
        sortCostBoard()
    }

    /**
     * Queries the economic data of a specific user.
     * Initializes the user data if not present.
     *
     * @param user The user to query.
     * @return UserData for the requested user.
     */
    fun query(user: User): UserData {
        initUserData(user)
        return userData[user.idLong]!!
    }

    /**
     * Updates the stored data for a specific user.
     *
     * @param user The user data to update.
     */
    fun update(user: UserData) {
        update(user.id, user.money, user.cost)
    }

    /**
     * Sorts the leaderboard based on money.
     */
    fun sortMoneyBoard() {
        moneyBoard.sortByDescending { it.money }
    }

    /**
     * Sorts the leaderboard based on cost.
     */
    fun sortCostBoard() {
        costBoard.sortByDescending { it.money }
    }

    fun getEmbedBuilder(
        type: Type,
        embedBuilder: EmbedBuilder,
        fieldSetting: MessageDataSerializer.EmbedSetting.FieldSetting,
        substitutor: Substitutor
    ): EmbedBuilder {
        val board = when (type) {
            Type.Money -> moneyBoard
            Type.Cost -> costBoard
        }

        val count = min(board.size, min(config.board_user_show_limit, 25))

        return embedBuilder.apply {
            clearFields()
            for (i in 1..count) {
                val data = board[i - 1]

                addField(
                    substitutor.parse(fieldSetting.name)
                        .replace("%index%", "$i")
                        .replace("%name%", getUserById(data.id).name),
                    substitutor.parse(fieldSetting.value).replace(
                        "%economy_board%", "${if (type == Type.Money) data.money else data.cost}"
                    ),
                    fieldSetting.inline
                )
            }
        }
    }

    /**
     * Initializes user data if it does not exist in the system.
     *
     * @param user The user for whom data needs to be initialized.
     */
    private fun initUserData(user: User) {
        val id = user.idLong
        if (!userData.containsKey(id)) {
            val data = UserData(id, name = user.name)
            userData[id] = data
            moneyBoard.add(data)
            costBoard.add(data)

            val obj = json.computeIfAbsent(id.toString(), JsonObject()).asJsonObject
            obj.addProperty("money", 0)
            obj.addProperty("cost", 0)
            json.save()
        }
    }

    /**
     * Updates the name in the user data to reflect any changes.
     *
     * @param user The user whose name needs updating.
     */
    fun nameUpdate(user: User) {
        userData[user.idLong]?.name = user.name
    }

    /**
     * Updates the JSON file with the latest user money and cost data.
     *
     * @param userId The ID of the user to update.
     * @param money The updated amount of money.
     * @param cost The updated amount of cost.
     */
    private fun update(userId: Long, money: Int, cost: Int) {
        val obj = json.getAsJsonObject(userId.toString())
        obj.addProperty("money", money)
        obj.addProperty("cost", cost)
        json.save()
    }
}
