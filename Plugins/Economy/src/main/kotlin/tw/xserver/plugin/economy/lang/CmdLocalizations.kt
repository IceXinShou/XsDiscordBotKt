package tw.xserver.plugin.economy.lang

import tw.xserver.loader.localizations.LocalTemplate
import tw.xserver.loader.localizations.LocaleData
import tw.xserver.loader.localizations.LocalTemplate as LocalTemplate1

internal object CmdLocalizations {
    val balance = CommandWithMemberLd()
    val topMoney = LocalTemplate.NDLocalData()
    val topCost = LocalTemplate.NDLocalData()
    val addMoney = CommandWithMemberValueLd()
    val removeMoney = CommandWithMemberValueLd()
    val setMoney = CommandWithMemberValueLd()
    val addCost = CommandWithMemberValueLd()
    val removeCost = CommandWithMemberValueLd()
    val setCost = CommandWithMemberValueLd()

    class CommandWithMemberLd {
        val options = OptionsLd()
        val name = LocaleData()
        val description = LocaleData()

        class OptionsLd {
            val member = LocalTemplate1.NDLocalData()
        }
    }

    class CommandWithMemberValueLd {
        val options = OptionsLd()
        val name = LocaleData()
        val description = LocaleData()

        class OptionsLd {
            val member = LocalTemplate1.NDLocalData()
            val value = LocalTemplate1.NDLocalData()
        }
    }
}