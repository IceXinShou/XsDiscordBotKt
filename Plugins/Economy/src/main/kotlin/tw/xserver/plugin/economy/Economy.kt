package tw.xserver.plugin.economy

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import tw.xserver.loader.builtin.placeholder.Placeholder
import tw.xserver.plugin.economy.Event.config
import tw.xserver.plugin.economy.Event.storageManager

object Economy {
    internal fun handleTopCommands(event: SlashCommandInteractionEvent) {
        if (checkPermission(event)) return

        event.hook.editOriginal(
            MessageReplier.replyBoard(
                event,
                if (event.name == "top-money") Type.Money else Type.Cost,
            )
        ).queue()
    }

    internal fun handleButtonBalance(event: ButtonInteractionEvent, hook: InteractionHook) {
        updatePapi(event.user, queryData(event.user))
        hook.editOriginal(MessageReplier.reply(event)).queue()
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
                data.addMoney(value)
                saveAndUpdate(event, data, before, "economy_money_before")
                storageManager.sortMoneyBoard()
            }

            "remove-money" -> {
                val beforeMoney = data.money
                val beforeCost = data.cost
                data.removeMoneyAddCost(value)
                saveAndUpdate(
                    event, data, beforeMoney, "economy_money_before", "economy_cost_before" to "$beforeCost"
                )
                storageManager.sortMoneyBoard()
                storageManager.sortCostBoard()
            }

            "set-money" -> {
                val before = data.money
                data.setMoney(value)
                saveAndUpdate(event, data, before, "economy_money_before")
                storageManager.sortMoneyBoard()
            }

            "add-cost" -> {
                val before = data.cost
                data.addCost(value)
                saveAndUpdate(event, data, before, "economy_cost_before")
                storageManager.sortCostBoard()
            }

            "remove-cost" -> {
                val beforeCost = data.cost
                data.removeCost(value)
                saveAndUpdate(event, data, beforeCost, "economy_cost_before")
                storageManager.sortCostBoard()
            }

            "set-cost" -> {
                val before = data.cost
                data.setCost(value)
                saveAndUpdate(event, data, before, "economy_cost_before")
                storageManager.sortCostBoard()
            }
        }
    }

    private fun updatePapi(user: User, data: UserData, map: Map<String, String> = emptyMap()) {
        Placeholder.update(user, hashMapOf(
            "economy_money" to "${data.money}", "economy_cost" to "${data.cost}"
        ).apply { putAll(map) })
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

    private fun queryData(user: User): UserData = storageManager.query(user)

    private fun saveData(data: UserData) = storageManager.update(data)

    private fun getTargetUser(event: SlashCommandInteractionEvent): User {
        return if (config.adminId.none { it == event.user.idLong }) event.user
        else event.getOption("member")?.asUser ?: event.user
    }

    private fun checkPermission(event: SlashCommandInteractionEvent): Boolean {
        if (config.adminId.none { it == event.user.idLong }) {
            event.hook.editOriginal(
                MessageReplier.reply(event)
            ).queue()
            return true
        }
        return false
    }

    internal enum class Type {
        Money, Cost
    }
}