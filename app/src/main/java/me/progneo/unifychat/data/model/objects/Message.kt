package me.progneo.unifychat.data.model.objects

import me.progneo.unifychat.data.model.interfaces.ICompanion
import me.progneo.unifychat.data.model.interfaces.IMessageContent
import java.io.Serializable

data class Message(
    var id: Long = 0,
    val conversationId: Long = 0,
    var timeStamp: Long = 0,
    val sender: ICompanion? = null,
    val isOutgoing: Boolean = false,
    val replyToMessage: Message? = null,
    val forwardedMessages: List<Message>? = null,
    var content: IMessageContent = MessageText(),
    var canBeEdited: Boolean = false,
    var canBeDeletedOnlyForSelf: Boolean = false,
    var canBeDeletedForAllUsers: Boolean = false,
    var messenger: String = "",
) : Serializable
