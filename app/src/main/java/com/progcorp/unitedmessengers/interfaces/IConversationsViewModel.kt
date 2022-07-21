package com.progcorp.unitedmessengers.interfaces

import androidx.lifecycle.MediatorLiveData
import com.progcorp.unitedmessengers.data.model.Conversation

interface IConversationsViewModel {
    val conversationsList: MediatorLiveData<MutableList<Conversation>>
    fun selectConversationPressed(conversation: Conversation)
}