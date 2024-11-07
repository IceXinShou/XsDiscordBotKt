package tw.xserver.plugin.feedbacker.lang

import tw.xserver.loader.localizations.LocaleData

internal object CmdLocalizations {
    val feedbacker = CommandWithMemberLd()

    class ND {
        val name = LocaleData()
        val description = LocaleData()
    }

    class CommandWithMemberLd {
        val options = OptionsLd()
        val name = LocaleData()
        val description = LocaleData()

        class OptionsLd {
            val member = ND()
        }
    }
}