package tw.xserver.plugin.economy

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import tw.xserver.loader.builtin.placeholder.Placeholder
import tw.xserver.loader.builtin.placeholder.Substitutor
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
        updatePapi(event.user)
        hook.editOriginal(
            MessageReplier.getMessageEditData(event, event.userLocale, Placeholder.getSubstitutor(event.user))
        ).queue()
    }

    internal fun handleBalance(
        event: SlashCommandInteractionEvent
    ) {
        val targetUser = getTargetUser(event)
        updatePapi(targetUser)
        event.hook.editOriginal(
            MessageReplier.getMessageEditData(event, event.userLocale, Placeholder.getSubstitutor(targetUser))
        ).queue()
    }

    internal fun handleMoneyAndCostCommands(
        event: SlashCommandInteractionEvent
    ) {
        if (checkPermission(event)) return
        val value: Int = event.getOption("value", 0) { it.asInt }
        if (checkValue(value, event)) return

        val targetUser = getTargetUser(event)
        val data = queryData(targetUser)
        when (event.name) {
            "add-money" -> {
                val before = data.money
                data.addMoney(value)
                saveAndUpdate(targetUser, data, "economy_money_before" to "$before")
                reply(event, Placeholder.getSubstitutor(targetUser))
                storageManager.sortMoneyBoard()
            }

            "remove-money" -> {
                val beforeMoney = data.money
                val beforeCost = data.cost
                data.removeMoneyAddCost(value)
                saveAndUpdate(
                    targetUser, data,
                    "economy_money_before" to "$beforeMoney",
                    "economy_cost_before" to "$beforeCost"
                )
                reply(event, Placeholder.getSubstitutor(targetUser))
                storageManager.sortMoneyBoard()
                storageManager.sortCostBoard()
            }

            "set-money" -> {
                val before = data.money
                data.setMoney(value)
                saveAndUpdate(targetUser, data, "economy_money_before" to "$before")
                reply(event, Placeholder.getSubstitutor(targetUser))
                storageManager.sortMoneyBoard()
            }

            "add-cost" -> {
                val before = data.cost
                data.addCost(value)
                saveAndUpdate(targetUser, data, "economy_cost_before" to "$before")
                reply(event, Placeholder.getSubstitutor(targetUser))
                storageManager.sortCostBoard()
            }

            "remove-cost" -> {
                val before = data.cost
                data.removeCost(value)
                saveAndUpdate(targetUser, data, "economy_cost_before" to "$before")
                reply(event, Placeholder.getSubstitutor(targetUser))
                storageManager.sortCostBoard()
            }

            "set-cost" -> {
                val before = data.cost
                data.setCost(value)
                saveAndUpdate(targetUser, data, "economy_cost_before" to "$before")
                reply(event, Placeholder.getSubstitutor(targetUser))
                storageManager.sortCostBoard()
            }
        }
    }

    private fun updatePapi(user: User) {
        val data = queryData(user)
        Placeholder.update(
            user, hashMapOf(
                "economy_money" to "${data.money}", "economy_cost" to "${data.cost}"
            )
        )
    }

    private fun updatePapi(user: User, data: UserData, map: Map<String, String> = emptyMap()) {
        Placeholder.update(user, hashMapOf(
            "economy_money" to "${data.money}", "economy_cost" to "${data.cost}"
        ).apply { putAll(map) })
    }


    private fun saveAndUpdate(
        user: User,
        data: UserData,
        vararg placeholders: Pair<String, String>
    ) {
        saveData(data)
        updatePapi(user, data, placeholders.toMap())

    }

    private fun reply(event: SlashCommandInteractionEvent, substitutor: Substitutor) {
        event.hook.editOriginal(
            MessageReplier.getMessageEditData(event, event.userLocale, substitutor)
        ).queue()
    }

    private fun checkValue(value: Int, event: SlashCommandInteractionEvent): Boolean {
        if (value >= 0) return false

        event.hook.editOriginal("Bad Value").queue()
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
                MessageReplier.getNoPermissionMessageData(event.userLocale)
            ).queue()
            return true
        }
        return false
    }

    internal enum class Type {
        Money, Cost
    }
}