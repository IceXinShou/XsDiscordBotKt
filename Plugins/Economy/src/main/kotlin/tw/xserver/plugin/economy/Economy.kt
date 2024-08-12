package tw.xserver.plugin.economy

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.user.update.UserUpdateGlobalNameEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.xserver.loader.localizations.LangManager
import tw.xserver.loader.plugin.PluginEvent
import tw.xserver.loader.util.FileGetter
import tw.xserver.loader.util.json.JsonObjFileManager
import tw.xserver.plugin.economy.cmd.getGuildCommands
import tw.xserver.plugin.economy.googlesheet.SheetManager
import tw.xserver.plugin.economy.json.JsonManager
import tw.xserver.plugin.economy.lang.LangFileSerializer
import tw.xserver.plugin.economy.lang.Localizations
import tw.xserver.plugin.economy.setting.MainConfigSerializer
import tw.xserver.plugin.placeholder.PAPI
import java.io.File
import java.io.IOException

/**
 * Main class for the Economy plugin managing configurations, commands, and data handling.
 */
object Economy : PluginEvent(true) {
    private val MODE = Mode.Json
    private val logger: Logger = LoggerFactory.getLogger(Economy::class.java)
    internal const val DIR_PATH = "./plugins/Economy/"
    internal lateinit var config: MainConfigSerializer

    override fun load() {
        reloadAll()
    }

    override fun unload() {}

    override fun reloadConfigFile() {
        getter = FileGetter(DIR_PATH, Economy::class.java)

        try {
            getter.readInputStream("./config.yml").use {
                config = Yaml().decodeFromStream<MainConfigSerializer>(it)
            }
        } catch (e: IOException) {
            logger.error("Please configure ${DIR_PATH}./config.yml", e)
        }

        logger.info("Setting file loaded successfully")
        if (File(DIR_PATH, "data").mkdirs()) {
            logger.info("Default data folder created")
        }

        if (MODE == Mode.Json)
            JsonManager.json = JsonObjFileManager(File(DIR_PATH, "data/data.json"))
        logger.info("Data file loaded successfully")
    }

    override fun reloadLang() {
        LangManager(
            getter,
            DiscordLocale.CHINESE_TAIWAN,
            LangFileSerializer::class,
            Localizations::class
        )
    }

    override fun guildCommands(): Array<CommandData> = getGuildCommands()

    /**
     * Initializes data handling when the bot is ready.
     */
    override fun onReady(event: ReadyEvent) {
        if (MODE == Mode.Json)
            JsonManager.initFile()
        updateMoneyBoard()
        updateCostBoard()
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val locale: DiscordLocale = event.userLocale

        if (event.name.startsWith("top-")) {
            handleTopCommands(event, locale)
            return
        }

        val user = getTargetUser(event)
        val data = queryData(user)

        when (event.name) {
            "balance" -> handleBalance(event, user, data, locale)
            "add-money", "remove-money", "set-money", "set-cost" -> handleMoneyAndCostCommands(
                event,
                user,
                data,
                locale
            )
        }
    }

    private fun handleTopCommands(event: SlashCommandInteractionEvent, locale: DiscordLocale) {
        if (checkPermission(event)) return

        val interactType = when (event.name) {
            "top-money" -> InteractType.TopMoney
            "top-cost" -> InteractType.TopCost
            else -> return  // impossible
        }

        event.hook.editOriginal(
            MessageReplier.replyBoard(
                event.user,
                interactType,
                if (interactType == InteractType.TopMoney) Type.Money else Type.Cost,
                MODE,
                locale
            )
        ).queue()
    }

    private fun handleBalance(
        event: SlashCommandInteractionEvent,
        user: User,
        data: UserData,
        locale: DiscordLocale
    ) {
        updatePapi(user, data)
        event.hook.editOriginal(MessageReplier.reply(user, InteractType.Balance, locale)).queue()
    }

    private fun handleMoneyAndCostCommands(
        event: SlashCommandInteractionEvent,
        user: User,
        data: UserData,
        locale: DiscordLocale
    ) {
        if (checkPermission(event)) return
        val value: Int = event.getOption("value", 0) { it.asInt }
        if (checkValue(value, event)) return

        when (event.name) {
            "add-money" -> {
                val before = data.money
                data.add(value)
                saveAndUpdate(event, user, data, before, "economy_money_before", InteractType.AddMoney, locale)
                updateMoneyBoard()
            }

            "remove-money" -> {
                val beforeMoney = data.money
                val beforeCost = data.cost
                data.remove(value)
                saveAndUpdate(
                    event,
                    user,
                    data,
                    beforeMoney,
                    "economy_money_before",
                    InteractType.RemoveMoney,
                    locale,
                    "economy_cost_before" to "$beforeCost"
                )
                updateMoneyBoard()
                updateCostBoard()
            }

            "set-money" -> {
                val before = data.money
                data.setMoney(value)
                saveAndUpdate(event, user, data, before, "economy_money_before", InteractType.SetMoney, locale)
                updateMoneyBoard()
            }

            "set-cost" -> {
                val before = data.cost
                data.cost = value
                saveAndUpdate(event, user, data, before, "economy_cost_before", InteractType.SetCost, locale)
                updateCostBoard()
            }
        }
    }

    private fun saveAndUpdate(
        event: SlashCommandInteractionEvent,
        user: User,
        data: UserData,
        before: Int,
        key: String,
        interactType: InteractType,
        locale: DiscordLocale,
        vararg placeholders: Pair<String, String>
    ) {
        saveData(data)
        updatePapi(user, data, mapOf(key to "$before") + placeholders)
        event.hook.editOriginal(MessageReplier.reply(user, interactType, locale)).queue()
    }


    /**
     * Updates user's global name changes in the data storage.
     */
    override fun onUserUpdateGlobalName(event: UserUpdateGlobalNameEvent) = JsonManager.nameUpdate(event.user)

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
        PAPI.update(
            user, hashMapOf(
                "economy_money" to "${data.money}",
                "economy_cost" to "${data.cost}"
            ).apply { putAll(map) }
        )
    }

    private fun updateMoneyBoard() {
        if (MODE == Mode.Json) JsonManager.sortMoneyBoard()
    }

    private fun updateCostBoard() {
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
                MessageReplier.reply(event.user, InteractType.NoPermission, event.userLocale)
            ).queue()
            return true
        }
        return false
    }

    enum class InteractType(val value: String) {
        Balance("balance"),
        TopMoney("top_money"),
        TopCost("top_cost"),
        AddMoney("add_money"),
        RemoveMoney("remove_money"),
        SetMoney("set_money"),
        SetCost("set_cost"),
        NoPermission("no_permission"),
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
