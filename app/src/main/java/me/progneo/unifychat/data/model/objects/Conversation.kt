package me.progneo.unifychat.data.model.objects

import me.progneo.unifychat.data.model.interfaces.ICompanion
import me.progneo.unifychat.data.model.objects.companions.Bot
import me.progneo.unifychat.data.model.objects.companions.Chat
import me.progneo.unifychat.data.model.objects.companions.User
import java.io.Serializable

data class Conversation(
    val id: Long = 0,
    val companion: ICompanion? = null,
    var lastMessage: Message? = null,
    var unreadCount: Int = 0,
    var canWrite: Boolean = true,
    val messenger: String = ""
) : Serializable {

    fun getIsOnline(): Boolean {
        return when (companion) {
            is User -> {
                companion.isOnline
            }
            else -> {
                false
            }
        }
    }

    fun getLastOnline(): Long? {
        return if (companion is User) {
            companion.lastSeen
        } else null
    }

    fun getPhoto(): String {
        return when (companion) {
            is User -> {
                companion.photo
            }
            is Chat -> {
                companion.photo
            }
            is Bot -> {
                companion.photo
            }
            else -> {
                "Unreachable"
            }
        }
    }
}
