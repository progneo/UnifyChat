package com.progcorp.unitedmessengers.interfaces

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel

interface IClient {
    val currentConversation: LiveData<Conversation?>

    val conversationsList: MediatorLiveData<MutableList<Conversation>>
    val messagesList: MediatorLiveData<MutableList<Message>>

    var conversationViewModel: ConversationViewModel?

    fun setConversation(conversation: Conversation?)

    suspend fun loadLatestMessages()
    suspend fun loadMessagesFromId(messageId: Long)
    suspend fun sendMessage(message: Message)
    suspend fun editMessage(message: Message)
    suspend fun deleteMessages(messages: List<Message>, forAll: Boolean)
}