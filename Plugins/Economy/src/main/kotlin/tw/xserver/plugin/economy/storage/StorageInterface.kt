package tw.xserver.plugin.economy.storage

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import tw.xserver.plugin.creator.message.serializer.MessageDataSerializer
import tw.xserver.plugin.economy.Economy.Type
import tw.xserver.plugin.economy.UserData
import tw.xserver.plugin.placeholder.Substitutor

internal interface StorageInterface {
    fun init()
    fun query(user: User): UserData
    fun update(data: UserData)
    fun getEmbedBuilder(
        type: Type,
        embedBuilder: EmbedBuilder,
        fieldSetting: MessageDataSerializer.EmbedSetting.FieldSetting,
        substitutor: Substitutor
    ): EmbedBuilder

    fun sortMoneyBoard()
    fun sortCostBoard()
    fun nameUpdate(user: User)
}