package tw.xserver.plugin.basiccalculator.lang

import tw.xserver.loader.localizations.LocalTemplate
import tw.xserver.loader.localizations.LocaleData

internal object CmdLocalizations {
    val basicCalculate = CommandWithMemberLd()

    class CommandWithMemberLd {
        val name = LocaleData()
        val description = LocaleData()
        val options = OptionsLd()

        class OptionsLd {
            val formula = LocalTemplate.NDLocalData()
        }
    }
}