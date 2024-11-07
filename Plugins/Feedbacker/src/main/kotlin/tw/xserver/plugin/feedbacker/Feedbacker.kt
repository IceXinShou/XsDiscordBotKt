package tw.xserver.plugin.feedbacker

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import tw.xserver.loader.builtin.placeholder.Placeholder
import tw.xserver.plugin.creator.message.MessageCreator
import tw.xserver.plugin.creator.message.ModalCreator
import tw.xserver.plugin.feedbacker.Event.COMPONENT_PREFIX
import tw.xserver.plugin.feedbacker.Event.PLUGIN_DIR_FILE
import tw.xserver.plugin.feedbacker.Event.config
import tw.xserver.plugin.feedbacker.Event.globalLocale
import java.io.File
import java.util.*

object Feedbacker {
    private val messageCreator =
        MessageCreator(File(PLUGIN_DIR_FILE, "lang"), DiscordLocale.CHINESE_TAIWAN, COMPONENT_PREFIX)
    private val modalCreator =
        ModalCreator(File(PLUGIN_DIR_FILE, "lang"), DiscordLocale.CHINESE_TAIWAN, COMPONENT_PREFIX)

    lateinit var guild: Guild
    lateinit var submitChannel: TextChannel

    fun handleCommand(event: SlashCommandInteractionEvent) {
        if (!event.isFromGuild || event.guild!!.idLong != config.guildId) return
        if (Collections.disjoint(config.allowRoleId, event.member!!.roles.map { it.idLong })) {
            event.hook.editOriginal(config.formNoPermission).queue()
            return
        }

        val targetMember = event.getOption("member") { it.asMember }!!
        val locale = event.userLocale
        event.channel.sendMessage(
            messageCreator.getCreateBuilder("ask", locale, Placeholder.getSubstitutor(targetMember)).build()
        ).queue()

        event.hook.editOriginal(config.formSuccess).queue()
    }

    fun handleStarBtn(event: ButtonInteractionEvent) {
        val componentArgs = event.componentId.split(':')
        if (componentArgs.last() != event.user.id) {
            event.reply(config.formNotYou).setEphemeral(true).queue()
            return
        }

        val newComponents = event.message.components.map { actionRow ->
            ActionRow.of(
                actionRow.components.map { component ->
                    if (component is Button) {
                        if (component.id == event.componentId) {
                            component.withStyle(ButtonStyle.PRIMARY)
                        } else {
                            component.withStyle(ButtonStyle.SECONDARY)
                        }
                    } else {
                        component
                    }
                }
            )
        }

        event.hook.editOriginalComponents(newComponents).queue()
        event.deferEdit().queue()
    }


    fun handleFormBtn(event: ButtonInteractionEvent) {
        val componentArgs = event.componentId.split(':')
        if (componentArgs.last() != event.user.id) {
            event.reply(config.formNotYou).setEphemeral(true).queue()
            return
        }
        val locale = event.userLocale
        var stars: Int = -1

        event.message.components[0].forEachIndexed { index, button ->
            if ((button as Button).style == ButtonStyle.PRIMARY) {
                stars = index + 1
                return@forEachIndexed
            }
        }

        if (stars == -1) {
            event.reply(config.formWarning).setEphemeral(true).queue()
            return
        }

        event.replyModal(
            modalCreator.getModalBuilder(
                "form",
                locale,
                Placeholder.getSubstitutor(event).put("stars", stars.toString())
            ).build()
        ).queue()
    }

    fun handleSubmit(event: ModalInteractionEvent) {
        val componentArgs = event.modalId.split(':')
        val stars = componentArgs.last()[0] - '0'

        val substitutor = Placeholder.getSubstitutor(event.member!!)
            .putAll(
                "fb_stars" to "${"★ ".repeat(stars)}${"☆ ".repeat(5 - stars)}",
                "fb_content" to event.getValue("form")!!.asString
            )

        submitChannel.sendMessage(
            messageCreator.getCreateBuilder("submit-result", globalLocale, substitutor).build()
        ).queue()
        event.deferEdit().queue()
    }
}
