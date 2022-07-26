package com.progcorp.unitedmessengers.data.model

import android.os.Handler
import androidx.lifecycle.MediatorLiveData
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.interfaces.IMessagesList
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel
import com.progcorp.unitedmessengers.util.addFrontItem
import com.progcorp.unitedmessengers.util.addNewItem
import com.progcorp.unitedmessengers.util.updateItemAt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class VKMessagesList(
    override val conversation: Conversation,
    override val conversationViewModel: ConversationViewModel
) : IMessagesList {

    override val messagesList = MediatorLiveData<MutableList<Message>>()

    private val _client = App.application.vkClient
    private val _repository = _client.repository

    private var _job: Job? = null

    private var _handler = Handler()
    private var _messagesGetter: Runnable = Runnable {  }

    init { startHandlers() }

    override fun startHandlers() {
        _messagesGetter = Runnable {
            MainScope().launch {
                loadLatestMessages()
            }
            _handler.postDelayed(_messagesGetter, 3000)
        }
        _handler.postDelayed(_messagesGetter, 0)
    }

    override fun stopHandlers() {
        _handler.removeCallbacks(_messagesGetter)
    }

    override fun addNewMessage(message: Message) {
        messagesList.value?.find { it.id == message.id }?.let {
            messagesList.updateItemAt(it, messagesList.value!!.indexOf(it))
        } ?: run {
            messagesList.addFrontItem(message)
        }
        messagesList.value?.sortByDescending { it.id }
    }

    override fun addOldMessage(message: Message) {
        messagesList.value?.find { it.id == message.id }?.let {
            messagesList.updateItemAt(it, messagesList.value!!.indexOf(it))
        } ?: run {
            messagesList.addNewItem(message)
        }
        messagesList.value?.sortByDescending { it.id }
    }

    override suspend fun loadLatestMessages() {
        val data = _repository.getMessages(conversation.id,20).first()
        for (message in data) {
            addNewMessage(message)
        }
        messagesList.value?.let {
            if (it.size > 0) {
                markAsRead(it[0])
            }
        }
    }

    override suspend fun loadMessagesFromId(messageId: Long) {
        val data = _repository.getMessagesFromId(conversation.id, messageId, 20).first()
        for (message in data) {
            addOldMessage(message)
        }
    }

    override suspend fun sendMessage(message: Message) {
        MainScope().launch(Dispatchers.Main) {
            addNewMessage(message)
            val data = _repository.sendMessage(conversation.id, message).first()
            message.id = data
        }
    }

    override suspend fun editMessage(message: Message) {
        _repository.editMessage(conversation, message).first()
    }

    override suspend fun deleteMessages(messages: List<Message>, forAll: Boolean) {
        _repository.deleteMessages(messages, forAll).first()
    }

    override suspend fun updateMessageContent(update: Any) {

    }

    override suspend fun markAsRead(message: Message) {
        _repository.markAsRead(conversation.id, message).first()
    }
}