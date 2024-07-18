package tw.xserver.plugin.economy.lang

import tw.xserver.loader.localizations.LocaleData

internal object Localizations {
    val balance = CommandWithMember_lz()
    val top_money = ND()
    val top_cost = ND()
    val add_money = CommandWithMemberValue_lz()
    val remove_money = CommandWithMemberValue_lz()
    val set_money = CommandWithMemberValue_lz()
    val set_cost = CommandWithMemberValue_lz()

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