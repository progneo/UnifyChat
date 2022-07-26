package com.progcorp.unitedmessengers.interfaces

import androidx.lifecycle.MediatorLiveData
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel

interface IMessagesList {
    val conversation: Conversation
    val messagesList: MediatorLiveData<MutableList<Message>>
    val conversationViewModel: ConversationViewModel

    fun startHandlers()
    fun stopHandlers()
    fun addNewMessage(message: Message)
    fun addOldMessage(message: Message)
    suspend fun loadLatestMessages()
    suspend fun loadMessagesFromId(messageId: Long)
    suspend fun sendMessage(message: Message)
    suspend fun editMessage(message: Message)
    suspend fun deleteMessages(messages: List<Message>, forAll: Boolean)
    suspend fun updateMessageContent(update: Any)
    suspend fun markAsRead(message: Message)
}