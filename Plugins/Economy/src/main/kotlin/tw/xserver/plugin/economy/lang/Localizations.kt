package tw.xserver.plugin.economy.lang

import tw.xserver.loader.localizations.LocaleData

internal object Localizations {
    val balance = CommandWithMemberLd()
    val topMoney = ND()
    val topCost = ND()
    val addMoney = CommandWithMemberValueLd()
    val removeMoney = CommandWithMemberValueLd()
    val setMoney = CommandWithMemberValueLd()
    val setCost = CommandWithMemberValueLd()

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

    class CommandWithMemberValueLd {
        val options = OptionsLd()
        val name = LocaleData()
        val description = LocaleData()

        class OptionsLd {
            val member = ND()
            val value = ND()
        }
    }
}