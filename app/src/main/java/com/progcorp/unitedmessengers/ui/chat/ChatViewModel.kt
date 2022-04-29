package com.progcorp.unitedmessengers.ui.chat

import android.annotation.SuppressLint
import android.os.Handler
import android.util.Log
import androidx.lifecycle.*
import com.progcorp.unitedmessengers.data.db.vk.Messages
import com.progcorp.unitedmessengers.data.db.vk.requests.VKSendMessageCommand
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.ui.DefaultViewModel
import com.progcorp.unitedmessengers.ui.interfaces.IConversationViewModel
import com.progcorp.unitedmessengers.util.ConvertTime
import com.progcorp.unitedmessengers.util.addFrontItem
import com.progcorp.unitedmessengers.util.addNewItem
import com.progcorp.unitedmessengers.util.updateItemAt
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import java.util.*

class ChatViewModelFactory(private val conversation: Conversation) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(conversation) as T
    }
}

class ChatViewModel(private val conversation: Conversation) :
    DefaultViewModel(), Messages.OnMessagesFetched, IConversationViewModel {

    private var _handler = Handler()
    private var _messagesGetter: Runnable = Runnable {  }

    private val _messages: Messages = Messages(this)

    private val _conversation: MutableLiveData<Conversation> = MutableLiveData()
    private val _addedMessage = MutableLiveData<Message>()
    private val _newMessage = MutableLiveData<Message>()

    val newMessageText = MutableLiveData<String?>()
    val messagesList = MediatorLiveData<MutableList<Message>>()
    val chat: LiveData<Conversation> = _conversation

    init {
        messagesList.addSource(_addedMessage) { newMessage ->
            val conversation = messagesList.value?.find { it.id == newMessage.id }
            if (conversation == null) {
                messagesList.addNewItem(newMessage)
            }
            else {
                messagesList.updateItemAt(newMessage, messagesList.value!!.indexOf(conversation))
            }
        }
        messagesList.addSource(_newMessage) { newMessage ->
            val conversation = messagesList.value?.find { it.id == newMessage.id }
            if (conversation == null) {
                messagesList.addFrontItem(newMessage)
            }
            else {
                messagesList.updateItemAt(newMessage, messagesList.value!!.indexOf(conversation))
            }
        }
        _conversation.value = conversation
        loadSelectedMessages(0)
        startListeners()
    }

    override fun startListeners() {
        _messagesGetter = Runnable {
            loadNewMessages()
            _handler.postDelayed(_messagesGetter, 3000)
        }
        _handler.postDelayed(_messagesGetter, 0)
    }

    override fun stopListeners() {
        _handler.removeCallbacks(_messagesGetter)
    }

    override fun loadSelectedMessages(offset: Int) {
        _messages.vkGetMessages(this.conversation, offset, 20, false)
    }

    override fun loadNewMessages() {
        _messages.vkGetMessages(this.conversation, 0, 20, true)
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

    override fun onNewMessage(message: Message) {
        _newMessage.value = message
    }

    override fun sendMessagePressed() {
        if (!newMessageText.value.isNullOrBlank()) {
            val message = Message(
                date = Date().time / 1000,
                time = ConvertTime.toTime(Date().time / 1000),
                peerId = conversation.id,
                out = true,
                text = newMessageText.value!!,
                type = Message.MESSAGE_OUT
            )
            onNewMessage(message)
            VK.execute(VKSendMessageCommand(conversation.id, newMessageText.value!!), object: VKApiCallback<Int> {
                @SuppressLint("SetTextI18n")
                override fun success(result: Int) {
                    message.id = result
                    Log.i(TAG, "Message sent")
                }
                override fun fail(error: Exception) {
                    Log.e(TAG, error.toString())
                }
            })
            newMessageText.value = null
        }
    }

    override fun loadMoreMessages() {
        loadSelectedMessages(messagesList.value!!.size)
    }

    companion object {
        const val TAG = "ChatViewModel"
    }
}
