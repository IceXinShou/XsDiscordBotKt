package tw.xserver.plugin.placeholder

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import tw.xserver.loader.base.MainLoader.jdaBot

object PAPI {
    val globalPlaceholder: Substitutor = Substitutor(
        "%", "%",
        hashMapOf(
            "bot_id" to jdaBot.selfUser.id,
            "bot_name" to jdaBot.selfUser.name,
        )
    )
    private val userPlaceholder: MutableMap<Long, Substitutor> = HashMap()


    fun update(user: User, kv: Map<String, String>) {
        get(user).put(kv)
    }

    fun get(user: User): Substitutor {
        return userPlaceholder.getOrPut(
            user.idLong
        ) {
            Substitutor(
                "%", "%",
                hashMapOf(
                    "user_id" to user.id,
                    "user_name" to user.name,
                    "user_avatar_url" to user.effectiveAvatarUrl,
                )
            )
        }
    }

    fun get(member: Member): Substitutor {
        return userPlaceholder.getOrPut(
            member.idLong,
        ) {
            Substitutor(
                "%", "%", hashMapOf(
                    "user_id" to member.id,
                    "user_name" to member.user.name,
                    "user_avatar_url" to member.user.effectiveAvatarUrl,
                    "member_nickname" to member.effectiveName,
                    "member_avatar_url" to member.effectiveAvatarUrl,
                )
            )
        }
    }
}