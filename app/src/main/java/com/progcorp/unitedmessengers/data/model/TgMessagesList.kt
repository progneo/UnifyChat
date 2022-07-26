package com.progcorp.unitedmessengers.data.model

import android.util.Log
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
import org.drinkless.td.libcore.telegram.TdApi

class TgMessagesList(
    override val conversation: Conversation,
    override val conversationViewModel: ConversationViewModel
) : IMessagesList {

    override val messagesList = MediatorLiveData<MutableList<Message>>()

    private val _client = App.application.tgClient
    private val _repository = _client.repository

    private var _job: Job? = null

    init { startHandlers() }

    override fun startHandlers() {
        _job = MainScope().launch(Dispatchers.Main) {
            _client.updateResult.collect { update ->
                Log.d("TgMessagesList", "onResult: ${update?.javaClass?.simpleName}")
                when (update) {
                    is TdApi.UpdateNewMessage -> {
                        if (update.message.chatId == conversation.id) {
                            val message = Message.tgParse(update.message)
                            addNewMessage(message)
                            markAsRead(message)
                        }
                    }
                    is TdApi.UpdateMessageSendSucceeded -> {
                        messagesList.value?.let { list ->
                            list.find { it.id == update.oldMessageId }?.let {
                                it.id = update.message.id
                                it.canBeEdited = update.message.canBeEdited
                                it.canBeDeletedForAllUsers = update.message.canBeDeletedForAllUsers
                                it.canBeDeletedOnlyForSelf = update.message.canBeDeletedOnlyForSelf
                            } ?: run {
                                addNewMessage(Message.tgParse(update.message))
                            }
                        }
                    }
                    is TdApi.UpdateMessageContent -> {
                        if (conversation.id == update.chatId) {
                            messagesList.value?.let { list ->
                                list.find { it.id == update.messageId }?.let { message ->
                                    message.updateMessageContent(update.newContent)
                                    conversationViewModel.messageEdited(list.indexOf(message))
                                }
                            }
                        }
                    }
                    is TdApi.UpdateUserStatus -> {
                        if (update.userId == conversation.id) {
                            MainScope().launch {
                                conversation.tgParseOnlineStatus(update)
                            }
                        }
                        conversationViewModel.updateConversation(conversation)
                    }
                    is TdApi.UpdateDeleteMessages -> {
                        if (update.chatId == conversation.id) {
                            if (!update.fromCache) {
                                for (id in update.messageIds) {
                                    messagesList.value?.let { list ->
                                        val message = list.find {
                                            it.id == id
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
        }
    }

    override fun stopHandlers() {
        _job?.cancel()
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
        val data = _repository.getMessages(conversation.id, 0,20).first()
        for (item in data) {
            addNewMessage(Message.tgParse(item))
        }
        messagesList.value?.let {
            if (it.size > 0) {
                markAsRead(it[0])
            }
        }
    }

    override suspend fun loadMessagesFromId(messageId: Long) {
        val data = _repository.getMessages(conversation.id, messageId, 20).first()
        for (item in data) {
            addOldMessage(Message.tgParse(item))
        }
    }

    override suspend fun sendMessage(message: Message) {
        MainScope().launch(Dispatchers.IO) {
            _repository.sendMessage(conversation.id, message).first()
        }
    }

    override suspend fun editMessage(message: Message) {
        when (message.content) {
            is MessageText -> {
                _repository.editMessageText(conversation.id, message).first()
            }
            is MessagePhoto -> {
                _repository.editMessageCaption(conversation.id, message).first()
            }
            is MessageAnimation -> {
                _repository.editMessageCaption(conversation.id, message).first()
            }
            is MessageVideo -> {
                _repository.editMessageCaption(conversation.id, message).first()
            }
            is MessageVoiceNote -> {
                _repository.editMessageCaption(conversation.id, message).first()
            }
            is MessageDocument -> {
                _repository.editMessageCaption(conversation.id, message).first()
            }
            else -> {}
        }
    }

    override suspend fun deleteMessages(messages: List<Message>, forAll: Boolean) {
        _repository.deleteMessages(conversation.id, messages, forAll).first()
    }

    override suspend fun updateMessageContent(update: Any) {
        if (update is TdApi.UpdateMessageContent) {
            if (conversation.id == update.chatId) {
                messagesList.value?.let { list ->
                    list.find { it.id == update.messageId }?.let { message ->
                        message.updateMessageContent(update.newContent)
                        conversationViewModel.messageEdited(list.indexOf(message))
                    }
                }
            }
        }
    }

    override suspend fun markAsRead(message: Message) {
        _repository.markAsRead(conversation.id, message).first()
    }
}