package com.progcorp.unitedmessengers.ui.chat

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.*
import com.progcorp.unitedmessengers.data.db.vk.VKMessages
import com.progcorp.unitedmessengers.data.db.vk.requests.VKSendMessageCommand
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.ui.DefaultViewModel
import com.progcorp.unitedmessengers.util.ConvertTime
import com.progcorp.unitedmessengers.util.addNewItem
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import java.util.*
import kotlin.collections.ArrayList

class ChatViewModelFactory(private val conversation: Conversation) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(conversation) as T
    }
}

class ChatViewModel(private val conversation: Conversation) : DefaultViewModel(), VKMessages.OnMessagesFetched {

    private val _messages: VKMessages = VKMessages(this)

    private val _conversation: MutableLiveData<Conversation> = MutableLiveData()
    private val _addedMessage = MutableLiveData<Message>()

    val newMessageText = MutableLiveData<String>()
    val messagesList = MediatorLiveData<MutableList<Message>>()
    val chat: LiveData<Conversation> = _conversation

    init {
        setupChat()
    }

    private fun setupChat() {
        _conversation.value = conversation
        loadAndObserveNewMessages(0)
    }

    private fun loadAndObserveNewMessages(offset: Int) {
        messagesList.addSource(_addedMessage) {
            messagesList.addNewItem(it)
        }
        _messages.getMessages(this.conversation, offset, 30, false)
    }

    override fun showMessages(messages: ArrayList<Message>, isNew: Boolean) {
        for (message in messages) {
            _addedMessage.value = message
        }
    }

    private fun onNewMessage(message: Message) {
        _addedMessage.value = message
    }

    fun sendMessagePressed() {
        if (!newMessageText.value.isNullOrBlank()) {
            onNewMessage(
                Message(
                    date = Date().time / 1000,
                    time = ConvertTime.toDateTime(Date().time / 1000),
                    peerId = conversation.id,
                    out = true,
                    text = newMessageText.value!!,
                    type = Message.MESSAGE_OUT
                )
            )
            VK.execute(VKSendMessageCommand(conversation.id, newMessageText.value!!), object: VKApiCallback<Int> {
                @SuppressLint("SetTextI18n")
                override fun success(result: Int) {
                    Log.i(TAG, "Message sent")
                }
                override fun fail(error: Exception) {
                    Log.e(TAG, error.toString())
                }
            })
            newMessageText.value = ""
        }
    }
    companion object {
        const val TAG = "ChatViewModel"
    }
}
