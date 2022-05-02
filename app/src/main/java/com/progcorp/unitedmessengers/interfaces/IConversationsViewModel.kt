package com.progcorp.unitedmessengers.interfaces

import com.progcorp.unitedmessengers.data.model.Conversation

interface IConversationsViewModel {
    fun selectConversationPressed(conversation: Conversation)
}