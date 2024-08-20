package tw.xserver.plugin.economy.storage

import com.google.api.services.sheets.v4.model.ValueRange
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import tw.xserver.loader.util.GlobalUtil.getUserById
import tw.xserver.plugin.api.google.sheet.SheetsService
import tw.xserver.plugin.api.google.sheet.serializer.AuthConfigSerializer
import tw.xserver.plugin.creator.message.serializer.MessageDataSerializer
import tw.xserver.plugin.economy.Event.DIR_PATH
import tw.xserver.plugin.economy.Economy.Type
import tw.xserver.plugin.economy.Event.config
import tw.xserver.plugin.economy.UserData
import tw.xserver.plugin.placeholder.Substitutor
import kotlin.math.min

/**
 * Manages interactions with a Google Sheets spreadsheet for economy-related data operations.
 */
internal object SheetManager {
    private val spreadsheet = SheetsService(
        AuthConfigSerializer(config.client_id, config.client_secret, config.port), DIR_PATH
    ).sheets.spreadsheets().values()

    /**
     * Queries the Google Sheet to retrieve or initialize economy data for a specific user.
     *
     * @param user The Discord user to query.
     * @return UserData containing the user's economy data.
     */
    fun query(user: User): UserData {
        val current = query()
        val index = current[0].indexOf(user.idLong)

        return if (index == -1)
            UserData(user.idLong, name = getUserById(user.idLong).name)
        else
            UserData(
                user.idLong,
                money = current[1][index].toString().toInt(),
                cost = current[2][index].toString().toInt()
            )
    }

    /**
     * Updates the spreadsheet with the latest user data.
     *
     * @param data The user data to be updated.
     */
    fun update(data: UserData) {
        update(data.id, data.money, data.cost)
    }

    /**
     * Constructs an embed builder to display a leaderboard based on user data type (Money or Cost).
     *
     * @param type The type of economic data to display.
     * @return EmbedBuilder configured to display the leaderboard.
     */
    fun getEmbedBuilder(
        type: Type,
        embedBuilder: EmbedBuilder,
        fieldSetting: MessageDataSerializer.EmbedSetting.FieldSetting,
        substitutor: Substitutor
    ): EmbedBuilder {
        val board = when (type) {
            Type.Money -> queryAll().sortedByDescending { it.money }
            Type.Cost -> queryAll().sortedByDescending { it.cost }
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
                        "%economy_board%",
                        "${if (type == Type.Money) data.money else data.cost}"
                    ),
                    fieldSetting.inline
                )
            }
        }
    }

    private fun update(userId: Long, money: Int, cost: Int) {
        val index = indexOfUserId(userId)
        if (index == -1) {
            // User not in the sheet, append new entry
            spreadsheet
                .append(
                    config.sheet_id, parseRange(config.sheet_range_id),
                    ValueRange().setValues(listOf(listOf("$userId", "$money", "$cost")))
                )
                .setValueInputOption("RAW")
                .execute()
        } else {
            // Update existing entry
            spreadsheet
                .update(
                    config.sheet_id,
                    parseRange(offsetRange(config.sheet_range_id, index)),
                    ValueRange().setValues(listOf(listOf("$userId", "$money", "$cost")))
                )
                .setValueInputOption("RAW")
                .execute()
        }
    }

    private fun query(): List<List<Long>> = getBatchRange(
        listOf(
            parseRange(config.sheet_range_id),
            parseRange(config.sheet_range_money),
            parseRange(config.sheet_range_cost),
        )
    )

    private fun queryAll(): List<UserData> {
        val data = query()
        return data[0].mapIndexed { index, id ->
            UserData(id, data[1][index].toInt(), data[2][index].toInt(), getUserById(id).name)
        }
    }

    private fun indexOfUserId(userId: Long): Int = getRange(config.sheet_range_id).indexOf(userId)

    private fun offsetRange(baseCell: String, offset: Int): String {
        val (column, row) = Regex("([A-Za-z]+)(\\d+)").find(baseCell)!!.destructured
        return "$column${row.toInt() + offset}"
    }

    private fun parseRange(range: String): String = "${config.sheet_label}!${range}"

    private fun getRange(range: String): List<Long> =
        (spreadsheet.get(config.sheet_id, parseRange(range)).execute().getValues() ?: listOf())
            .flatten()
            .map { it.toString().toLong() }

    private fun getBatchRange(ranges: List<String>): List<List<Long>> {
        val valueRanges = spreadsheet.batchGet(config.sheet_id).setRanges(ranges).execute().valueRanges
        return valueRanges.map { range ->
            range.getValues()?.flatten()?.map { it.toString().toLong() } ?: emptyList()
        }
    }
}
