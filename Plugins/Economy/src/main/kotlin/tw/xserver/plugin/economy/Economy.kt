package tw.xserver.plugin.economy

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import tw.xserver.plugin.economy.Event.MODE
import tw.xserver.plugin.economy.Event.config
import tw.xserver.plugin.economy.storage.JsonManager
import tw.xserver.plugin.economy.storage.SheetManager
import tw.xserver.plugin.placeholder.Placeholder

object Economy {
    internal fun handleTopCommands(event: SlashCommandInteractionEvent) {
        if (checkPermission(event)) return

        event.hook.editOriginal(
            MessageReplier.replyBoard(
                event.user,
                event,
                if (event.name == "top-money") Type.Money else Type.Cost,
                MODE,
            )
        ).queue()
    }

    internal fun handleBalance(
        event: SlashCommandInteractionEvent
    ) {
        updatePapi(event.user, queryData(getTargetUser(event)))
        event.hook.editOriginal(MessageReplier.reply(event)).queue()
    }

    internal fun handleMoneyAndCostCommands(
        event: SlashCommandInteractionEvent
    ) {
        if (checkPermission(event)) return
        val value: Int = event.getOption("value", 0) { it.asInt }
        if (checkValue(value, event)) return

        val data = queryData(getTargetUser(event))
        when (event.name) {
            "add-money" -> {
                val before = data.money
                data.add(value)
                saveAndUpdate(event, data, before, "economy_money_before")
                updateMoneyBoard()
            }

            "remove-money" -> {
                val beforeMoney = data.money
                val beforeCost = data.cost
                data.remove(value)
                saveAndUpdate(
                    event,
                    data,
                    beforeMoney,
                    "economy_money_before",
                    "economy_cost_before" to "$beforeCost"
                )
                updateMoneyBoard()
                updateCostBoard()
            }

            "set-money" -> {
                val before = data.money
                data.setMoney(value)
                saveAndUpdate(event, data, before, "economy_money_before")
                updateMoneyBoard()
            }

            "set-cost" -> {
                val before = data.cost
                data.cost = value
                saveAndUpdate(event, data, before, "economy_cost_before")
                updateCostBoard()
            }
        }
    }

    private fun saveAndUpdate(
        event: SlashCommandInteractionEvent,
        data: UserData,
        before: Int,
        key: String,
        vararg placeholders: Pair<String, String>
    ) {
        saveData(data)
        updatePapi(event.user, data, mapOf(key to "$before") + placeholders)
        event.hook.editOriginal(MessageReplier.reply(event)).queue()
    }


    private fun checkValue(value: Int, event: SlashCommandInteractionEvent): Boolean {
        if (value >= 0) return false

        event.hook.editOriginal("").queue()
        return true
    }

    private fun queryData(user: User): UserData = when (MODE) {
        Mode.Json -> JsonManager.query(user)
        Mode.GoogleSheet -> SheetManager.query(user)
    }

    private fun saveData(data: UserData) = when (MODE) {
        Mode.Json -> JsonManager.update(data)
        Mode.GoogleSheet -> SheetManager.update(data)
    }

    private fun updatePapi(user: User, data: UserData, map: Map<String, String> = emptyMap()) {
        Placeholder.update(
            user, hashMapOf(
                "economy_money" to "${data.money}",
                "economy_cost" to "${data.cost}"
            ).apply { putAll(map) }
        )
    }

    internal fun updateMoneyBoard() {
        if (MODE == Mode.Json) JsonManager.sortMoneyBoard()
    }

    internal fun updateCostBoard() {
        if (MODE == Mode.Json) JsonManager.sortCostBoard()
    }

    private fun getTargetUser(event: SlashCommandInteractionEvent): User {
        return if (config.admin_id.none { it == event.user.idLong })
            event.user
        else
            event.getOption("member")?.asUser ?: event.user

    }

    private fun checkPermission(event: SlashCommandInteractionEvent): Boolean {
        if (config.admin_id.none { it == event.user.idLong }) {
            event.hook.editOriginal(
                MessageReplier.reply(event)
            ).queue()
            return true
        }
        return false
    }

    internal enum class Mode {
        Json,
        GoogleSheet,
    }

    internal enum class Type {
        Money,
        Cost
    }
}