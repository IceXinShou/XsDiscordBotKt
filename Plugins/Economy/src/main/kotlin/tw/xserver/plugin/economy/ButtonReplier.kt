package tw.xserver.plugin.economy

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

internal object ButtonReplier {
    fun onQuery(event: ButtonInteractionEvent) {
        // handle "xs:economy:v2:balance"
        event.deferReply(true).queue {
            Economy.handleButtonBalance(event, it)
        }
    }
}