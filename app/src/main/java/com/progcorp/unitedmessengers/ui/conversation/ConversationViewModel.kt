package com.progcorp.unitedmessengers.ui.conversation

import android.os.Handler
import android.util.Log
import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.db.Messages
import com.progcorp.unitedmessengers.data.db.vk.requests.VKSendMessageRequest
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.ui.DefaultViewModel
import com.progcorp.unitedmessengers.util.ConvertTime
import com.progcorp.unitedmessengers.util.addFrontItem
import com.progcorp.unitedmessengers.util.addNewItem
import com.progcorp.unitedmessengers.util.updateItemAt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class ConversationViewModelFactory(private val conversation: Conversation) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConversationViewModel(conversation) as T
    }
}

class ConversationViewModel(private val conversation: Conversation) :
    DefaultViewModel(), Messages.OnMessagesFetched {

    private var _scope = MainScope()

    private var _handler = Handler()
    private var _messagesGetter: Runnable = Runnable {  }

    private val _messages: Messages = Messages(this)

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
        _messagesGetter = Runnable {
            loadNewMessages()
            _handler.postDelayed(_messagesGetter, 3000)
        }
        _handler.postDelayed(_messagesGetter, 0)
    }

    fun stopListeners() {
        _handler.removeCallbacks(_messagesGetter)
    }

    private fun loadSelectedMessages(offset: Int) {
        when (conversation.messenger) {
            "vk" -> {
                _scope.launch(Dispatchers.Main) {
                    _messages.vkGetMessages(conversation, offset, 20, false)
                }
            }
            "tg" -> {
                if (messagesList.value != null) {
                    _scope.launch(Dispatchers.Main) {
                        _messages.tgGetMessages(
                            conversation.id,
                            messagesList.value!![offset].id,
                            20,
                            false
                        )
                    }
                }
            }
        }
    }

    private fun loadNewMessages() {
        when (conversation.messenger) {
            "vk" -> {
                _scope.launch(Dispatchers.Main) {
                    _messages.vkGetMessages(conversation, 0, 20, true)
                }
            }

            "tg" -> {
                _scope.launch(Dispatchers.Main) {
                    _messages.tgGetMessages(
                        conversation.id,
                        0,
                        20,
                        false
                    )
                }
            }
        }
    }

    override fun showMessages(messages: ArrayList<Message>, isNew: Boolean) {
        if (!isNew) {
            for (message in messages) {
                _addedMessage.value = message
            }
        }
        else {
            for (message in messages) {
                _newMessage.value = message
            }
        }
        if (messagesList.value != null) {
            messagesList.value!!.sortByDescending {
                it.date
            }
        }
    }

    fun sendMessagePressed() {
        _scope.launch {
        if (!newMessageText.value.isNullOrBlank()) {
            val message = Message(
                date = Date().time,
                time = ConvertTime.toTime(Date().time),
                peerId = conversation.id,
                out = true,
                text = newMessageText.value!!,
                type = Message.MESSAGE_OUT
            )
            _newMessage.value = message

            when (conversation.messenger) {
                "vk" -> {
                    val response =
                        App.application.vkRetrofit.create(VKSendMessageRequest::class.java)
                            .messageSend(
                                App.application.vkAccountService.token!!,
                                "5.131",
                                conversation.id, newMessageText.value!!,
                                0,
                                0
                            )

                    val responseJson = JSONObject(response)
                    try {
                        message.id = responseJson.getLong("response")
                    } catch (ex: JSONException) {
                        Log.e(TAG, ex.stackTraceToString())
                    }
                }
                "tg" -> {

                }
            }

            newMessageText.value = null
            }
        }
    }

    fun backPressed() {
        _backEvent.value = Event(Unit)
    }

    fun loadMoreMessages() {
        loadSelectedMessages(messagesList.value!!.size - 1)
    }

    companion object {
        const val TAG = "ConversationViewModel"
    }
}
