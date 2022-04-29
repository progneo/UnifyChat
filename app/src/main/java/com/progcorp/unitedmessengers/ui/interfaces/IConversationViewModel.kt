package com.progcorp.unitedmessengers.ui.interfaces

import com.progcorp.unitedmessengers.data.model.Message

interface IConversationViewModel {
    fun startListeners()
    fun stopListeners()
    fun loadSelectedMessages(offset: Int)
    fun loadNewMessages()
    fun showMessages(messages: ArrayList<Message>, isNew: Boolean)
    fun onNewMessage(message: Message)
    fun sendMessagePressed()
    fun loadMoreMessages()
}