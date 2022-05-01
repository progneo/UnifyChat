package com.progcorp.unitedmessengers.ui.conversation

import android.os.Handler
import android.util.Log
import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.db.Conversations
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
import kotlinx.coroutines.runBlocking
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
    DefaultViewModel(), Messages.OnMessagesFetched, Conversations.OnConversationsFetched {

    private var _scope = MainScope()

    private var _handler = Handler()
    private var _messagesGetter: Runnable = Runnable {  }
    private var _conversationGetter: Runnable = Runnable {  }

    private val _messages: Messages = Messages(this)
    private val _conversations: Conversations = Conversations(this)

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
        loadSelectedMessages(0)
        startListeners()
    }

    private fun startListeners() {
        _messagesGetter = Runnable {
            loadNewMessages()
            _handler.postDelayed(_messagesGetter, 3000)
        }
        _conversationGetter = Runnable {
            updateConversation()
            _handler.postDelayed(_conversationGetter, 30000)
        }
        _handler.postDelayed(_conversationGetter, 30000)
        _handler.postDelayed(_messagesGetter, 0)
    }

    fun stopListeners() {
        _handler.removeCallbacks(_messagesGetter)
        _handler.removeCallbacks(_conversationGetter)
    }

    private fun loadSelectedMessages(offset: Int) {
        _scope.launch(Dispatchers.Main) {
            _messages.vkGetMessages(conversation, offset, 20, false)
        }
    }

    private fun loadNewMessages() {
        _scope.launch(Dispatchers.Main) {
            _messages.vkGetMessages(conversation, 0, 20, true)
        }
    }

    private fun updateConversation() {
        _scope.launch {
            _conversations.vkGetConversationById(conversation.id)
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
    }

    override fun showConversations(chats: ArrayList<Conversation>, isNew: Boolean) {
        _conversation.value = chats[0]
    }

    fun sendMessagePressed() = runBlocking {
        if (!newMessageText.value.isNullOrBlank()) {
        val message = Message(
            date = Date().time / 1000,
            time = ConvertTime.toTime(Date().time / 1000),
            peerId = conversation.id,
            out = true,
            text = newMessageText.value!!,
            type = Message.MESSAGE_OUT
        )
        _newMessage.value = message

        val response = App.application.vkRetrofit.create(VKSendMessageRequest::class.java)
            .messageSend(
                App.application.vkAccountService.token!!,
                "5.131",
                conversation.id, newMessageText.value!!,
                0,
                0
            )

        val responseJson = JSONObject(response)
        try {
            message.id = responseJson.getInt("response")
        } catch (ex: JSONException) {
            Log.e(TAG, ex.stackTraceToString())
        }

        newMessageText.value = null
    }}

    fun backPressed() {
        _backEvent.value = Event(Unit)
    }

    fun loadMoreMessages() {
        loadSelectedMessages(messagesList.value!!.size)
    }

    companion object {
        const val TAG = "ConversationViewModel"
    }
}
