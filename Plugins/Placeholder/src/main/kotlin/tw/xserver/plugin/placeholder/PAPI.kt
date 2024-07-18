package tw.xserver.plugin.placeholder

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

object PAPI {
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
                    "%%" to "%",
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
                    "%%" to "%",
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