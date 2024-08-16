package tw.xserver.plugin.economy.lang

import tw.xserver.loader.localizations.LocaleData

internal object Localizations {
    val balance = CommandWithMember_lz()
    val topMoney = ND()
    val topCost = ND()
    val addMoney = CommandWithMemberValue_lz()
    val removeMoney = CommandWithMemberValue_lz()
    val setMoney = CommandWithMemberValue_lz()
    val setCost = CommandWithMemberValue_lz()

    class ND {
        val name = LocaleData()
        val description = LocaleData()
    }

    class CommandWithMember_lz {
        val options = Options_lz
        val name = LocaleData()
        val description = LocaleData()

        object Options_lz {
            val member = ND()
        }
    }

    class CommandWithMemberValue_lz {
        val options = Options_lz
        val name = LocaleData()
        val description = LocaleData()

        object Options_lz {
            val member = ND()
            val value = ND()
        }
    }
}