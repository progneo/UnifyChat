package com.progcorp.unitedmessengers.data.model

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.interfaces.IMessagesList
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel
import com.progcorp.unitedmessengers.util.addFrontItem
import com.progcorp.unitedmessengers.util.addNewItem
import com.progcorp.unitedmessengers.util.updateItemAt
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class VKMessagesList(
    override val conversation: Conversation,
    override val conversationViewModel: ConversationViewModel
) : IMessagesList {

    override val messagesList = MediatorLiveData<MutableList<Message>>()

    private val _client = App.application.vkClient
    private val _repository = _client.repository

    private var _job: Job? = null

    init { startHandlers() }

    override fun startHandlers() {
        MainScope().launch {
            loadLatestMessages()
        }
        _job = MainScope().launch(Dispatchers.Main) {
            _client.updateResult.collect { update ->
                Log.d("VKMessagesList", "onResult: ${update?.javaClass?.simpleName}")
                when (update) {
                    is VKUpdateNewMessages -> {
                        for (message in update.messages) {
                            if (message.conversationId == conversation.id) {
                                addNewMessage(message)
                                markAsRead(message)
                            }
                        }
                    }
                    is VKUpdateMessagesContent -> {
                        for (message in update.messages) {
                            messagesList.value?.let { list ->
                                list.find {
                                    it.id == message.id
                                }?.let {
                                    list.indexOf(it).let { index ->
                                        messagesList.updateItemAt(message, index)
                                        conversationViewModel.messageEdited(index)
                                    }
                                }
                            }
                        }
                    }
                    is VKUpdateUserStatus -> {
                        if (update.userId == conversation.id) {
                            conversation.vkParseOnlineStatus(update)
                        }
                        conversationViewModel.updateConversation(conversation)
                    }
                    is VKUpdateDeleteMessage -> {
                        if (update.chatId == conversation.id) {
                            messagesList.value?.let { list ->
                                val message = list.find {
                                    it.id == update.messageId
                                }
                                val position = list.indexOf(message)
                                if (message != null) {
                                    list.remove(message)
                                }
                                conversationViewModel.messageDeleted(position)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun stopHandlers() {
        _job?.cancel()
    }

    override fun addNewMessage(message: Message) {
        Log.d("longPoll", message.toString())
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