package com.progcorp.unitedmessengers.interfaces

import android.view.View
import androidx.lifecycle.MediatorLiveData
import com.progcorp.unitedmessengers.data.model.Conversation

interface IConversationsViewModel {
    val conversationsList: MediatorLiveData<MutableList<Conversation>>
    fun selectConversationPressed(conversation: Conversation)
    fun longClickOnConversation(view: View, conversation: Conversation)
}