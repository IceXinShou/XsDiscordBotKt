package tw.xserver.plugin.logger.chat.lang

import tw.xserver.loader.localizations.LocaleData

internal object CmdLocalizations {
    val chatLogger = CommandWithMemberLd()

    class CommandWithMemberLd {
        val name = LocaleData()
        val description = LocaleData()
        val subcommands = SubCommandsLd()

        class SubCommandsLd {
            val setting = SimpleCommandLd()

            class SimpleCommandLd {
                val name = LocaleData()
                val description = LocaleData()
            }
        }
    }
}