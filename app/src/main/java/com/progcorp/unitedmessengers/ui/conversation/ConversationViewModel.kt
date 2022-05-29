@file:OptIn(ExperimentalCoroutinesApi::class)

package com.progcorp.unitedmessengers.ui.conversation

import android.os.Handler
import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.data.model.MessageText
import com.progcorp.unitedmessengers.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi
import java.util.*

class ConversationViewModelFactory(private val conversation: Conversation) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConversationViewModel(conversation) as T
    }
}

class ConversationViewModel(private val conversation: Conversation) : ViewModel()  {

    private var _scope = MainScope()

    private var _handler = Handler()
    private var _messagesGetter: Runnable = Runnable {  }

    private val _backEvent = MutableLiveData<Event<Unit>>()

    private val _conversation: MutableLiveData<Conversation> = MutableLiveData()
    private val _addedMessage = MutableLiveData<Message>()
    private val _newMessage = MutableLiveData<Message>()

    val backEvent: LiveData<Event<Unit>> = _backEvent

    val newMessageText = MutableLiveData<String?>()
    val messagesList = MediatorLiveData<MutableList<Message>>()
    val chat: LiveData<Conversation> = _conversation

    init {
        messagesList.addSource(_addedMessage) { newMessage ->
            val message = messagesList.value?.find { it.id == newMessage.id }
            if (message == null) {
                messagesList.addNewItem(newMessage)
            }
            else {
                messagesList.updateItemAt(newMessage, messagesList.value!!.indexOf(message))
            }
        }
        messagesList.addSource(_newMessage) { newMessage ->
            val message = messagesList.value?.find { it.id == newMessage.id }
            if (message == null) {
                messagesList.addFrontItem(newMessage)
            }
            else {
                messagesList.updateItemAt(newMessage, messagesList.value!!.indexOf(message))
            }
        }
        _conversation.value = conversation
        startListeners()
    }

    private fun startListeners() {
        when (conversation.messenger) {
            Constants.Messenger.VK -> {
                _messagesGetter = Runnable {
                    loadNewMessages()
                    _handler.postDelayed(_messagesGetter, 3000)
                }
                _handler.postDelayed(_messagesGetter, 0)
            }
            Constants.Messenger.TG -> {
                App.application.tgClient.conversationViewModel = this
            }
        }
        loadNewMessages()
    }

    fun stopListeners() {
        when (conversation.messenger) {
            Constants.Messenger.VK -> {
                _handler.removeCallbacks(_messagesGetter)
            }
            Constants.Messenger.TG -> {
                App.application.tgClient.conversationViewModel = null
            }
        }
    }

    private fun loadSelectedMessages(offset: Int) {
        when (conversation.messenger) {
            Constants.Messenger.VK -> {
                _scope.launch(Dispatchers.Main) {
                    App.application.vkRepository.getMessages(conversation, offset, 20)
                }
            }
            Constants.Messenger.TG -> {
                if (messagesList.value != null) {
                    _scope.launch(Dispatchers.Main) {
                        App.application.tgRepository.getMessages(
                            conversation.id,
                            messagesList.value!![offset].id,
                            20
                        )
                    }
                }
            }
        }
    }

    private fun loadNewMessages() {
        when (conversation.messenger) {
            Constants.Messenger.VK -> {
                _scope.launch(Dispatchers.Main) {
                    App.application.vkRepository.getMessages(conversation, 0, 20)
                }
            }

            Constants.Messenger.TG -> {
                _scope.launch(Dispatchers.Main) {
                    App.application.tgRepository.getMessages(conversation.id, 0,20)
                }
            }
        }
    }

    fun sendMessagePressed() {
        _scope.launch {
            if (!newMessageText.value.isNullOrBlank()) {
                val message = Message(
                    timeStamp = Date().time,
                    sender = chat.value!!.companion,
                    isOutgoing = true,
                    replyToMessageId = 0,
                    content = MessageText(newMessageText.value!!)
                )
                _newMessage.value = message
                newMessageText.value = null

                when (conversation.messenger) {
                    Constants.Messenger.VK -> {
                        App.application.vkRepository.sendMessage(chat.value!!.id, message).map {
                            message.id = it
                        }
                    }
                    Constants.Messenger.TG -> {
                        App.application.tgRepository.sendMessage(chat.value!!.id, message).map {
                            //message.id = it
                        }
                    }
                }
            }
        }
    }

    fun backPressed() {
        _backEvent.value = Event(Unit)
    }

    fun loadMoreMessages() {
        loadSelectedMessages(messagesList.value!!.size - 1)
    }

    fun updateOnline(data: TdApi.UpdateUserStatus) {
        if (data.userId == chat.value!!.id) {
            _scope.launch {
                _conversation.value!!.tgParseOnlineStatus(data)
            }
        }
    }

    fun newMessage(data: TdApi.UpdateNewMessage) {
        if (data.message.chatId == chat.value!!.id) {
            _scope.launch {
                _newMessage.value = Message.tgParse(data.message)
            }
        }
    }

    companion object {
        const val TAG = "ConversationViewModel"
    }
}
