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
import kotlinx.coroutines.flow.first
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

    private val _tgClient = App.application.tgClient
    private val _vkRepository = App.application.vkClient.repository

    private var _handler = Handler()
    private var _messagesGetter: Runnable = Runnable {  }

    private val _conversation: MutableLiveData<Conversation> = MutableLiveData()
    private val _addedMessage = MutableLiveData<Message>()
    private val _newMessage = MutableLiveData<Message>()

    private val _addAttachmentsPressed = MutableLiveData<Event<Unit>>()
    private val _toBottomPressed = MutableLiveData<Event<Unit>>()
    private val _onMessagePressed = MutableLiveData<Event<Message>>()

    private val _messageToReply = MutableLiveData<Event<Message>>()
    private val _messagesToForward = MutableLiveData<Event<List<Message>>>()
    private val _textToCopy = MutableLiveData<Event<String>>()
    private val _messageToEdit = MutableLiveData<Event<Message>>()
    private val _messageToDelete = MutableLiveData<Event<Message>>()

    val replyMessage = MutableLiveData<Message?>()
    val selectedMessage = MutableLiveData<Message?>()

    val newMessageText = MutableLiveData<String?>()
    val messagesList = MediatorLiveData<MutableList<Message>>()
    val chat: LiveData<Conversation> = _conversation

    val toBottomPressed: LiveData<Event<Unit>> = _toBottomPressed
    val addAttachmentPressed: LiveData<Event<Unit>> = _addAttachmentsPressed
    val onMessagePressed: LiveData<Event<Message>> = _onMessagePressed

    val messageToReply: LiveData<Event<Message>> = _messageToReply
    val messagesToForward: LiveData<Event<List<Message>>> = _messagesToForward
    val textToCopy: LiveData<Event<String>> = _textToCopy
    val messageToEdit: LiveData<Event<Message>> = _messageToEdit
    val messageToDelete: LiveData<Event<Message>> = _messageToDelete

    init {
        messagesList.addSource(_addedMessage) { newMessage ->
            val message = messagesList.value?.find { it.id == newMessage.id }
            if (message == null) {
                messagesList.addNewItem(newMessage)
            }
            else {
                messagesList.updateItemAt(newMessage, messagesList.value!!.indexOf(message))
            }
            messagesList.value?.sortByDescending {
                it.id
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
            messagesList.value?.sortByDescending {
                it.id
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
                _tgClient.conversationViewModel = this
            }
        }
        loadNewMessages()
    }

    private fun loadSelectedMessages(offset: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            when (conversation.messenger) {
                Constants.Messenger.VK -> {
                    val data = _vkRepository.getMessages(conversation, offset, 20).first()
                    for (message in data) {
                        _addedMessage.value = message
                    }
                }
                Constants.Messenger.TG -> {
                    if (messagesList.value != null) {
                        val data = _tgClient.repository.getMessages(
                            conversation.id,
                            messagesList.value!![offset].id,
                            20
                        ).first()
                        for (item in data) {
                            val message = Message.tgParse(item)
                            _addedMessage.value = message
                        }
                    }
                }
            }
        }
    }

    private fun loadNewMessages() {
        viewModelScope.launch {
            when (conversation.messenger) {
                Constants.Messenger.VK -> {
                    val data = _vkRepository.getMessages(conversation, 0, 20).first()
                    for (message in data) {
                        _newMessage.value = message
                    }
                }
                Constants.Messenger.TG -> {
                    val data = _tgClient.repository.getMessages(conversation.id, 0,20).first()
                    for (item in data) {
                        val message = Message.tgParse(item)
                        _newMessage.value = message
                    }
                }
            }
            if (messagesList.value != null) {
                markAsRead(messagesList.value!![0])
            }
        }
    }

    private fun markAsRead(message: Message) {
        viewModelScope.launch {
            when (conversation.messenger) {
                Constants.Messenger.VK -> {
                    _vkRepository.markAsRead(chat.value!!.id, message).first()
                }
                Constants.Messenger.TG -> {
                    _tgClient.repository.markAsRead(chat.value!!.id, message).first()
                }
            }
        }
    }

    fun stopListeners() {
        when (conversation.messenger) {
            Constants.Messenger.VK -> {
                _handler.removeCallbacks(_messagesGetter)
            }
            Constants.Messenger.TG -> {
                _tgClient.conversationViewModel = null
            }
        }
    }

    fun sendMessagePressed() {
        viewModelScope.launch {
            if (!newMessageText.value.isNullOrBlank()) {
                val message = Message(
                    timeStamp = Date().time,
                    sender = chat.value!!.companion,
                    isOutgoing = true,
                    replyToMessage = replyMessage.value,
                    content = MessageText(newMessageText.value!!)
                )
                newMessageText.value = null
                replyMessage.value = null

                when (conversation.messenger) {
                    Constants.Messenger.VK -> {
                        _newMessage.value = message
                        val data = _vkRepository.sendMessage(chat.value!!.id, message).first()
                        _newMessage.value?.id = data
                    }
                    Constants.Messenger.TG -> {
                        val data = _tgClient.repository.sendMessage(chat.value!!.id, message).first()
                        _newMessage.value = Message.tgParse(data)
                    }
                }
            }
        }
    }

    fun toBottomPressed() {
        _toBottomPressed.value = Event(Unit)
    }

    fun addAttachmentsPressed() {
        _addAttachmentsPressed.value = Event(Unit)
    }

    fun loadMoreMessages() {
        loadSelectedMessages(messagesList.value!!.size - 1)
    }

    fun updateOnline(data: TdApi.UpdateUserStatus) {
        if (data.userId == chat.value!!.id) {
            viewModelScope.launch(Dispatchers.Main) {
                _conversation.value!!.tgParseOnlineStatus(data)
                _conversation.postValue(_conversation.value!!.copy())
            }
        }
    }

    fun newMessage(data: TdApi.UpdateNewMessage) {
        if (data.message.chatId == chat.value!!.id) {
            viewModelScope.launch(Dispatchers.Main) {
                _newMessage.value = Message.tgParse(data.message)
                markAsRead(_newMessage.value!!)
            }
        }
    }

    fun onMessagePressed(message: Message) {
        selectedMessage.value = message
        _onMessagePressed.value = Event(message)
    }

    fun forwardMessage() {
        _messagesToForward.value = Event(listOf(selectedMessage.value!!))
    }

    fun replyOnMessage() {
        selectedMessage.value?.let {
            replyMessage.value = it
            _messageToReply.value = Event(it)
        }
    }

    fun copyTextToClipboard() {
        _textToCopy.value = Event(selectedMessage.value!!.content.text)
    }

    fun editMessage() {
        _messageToEdit.value = Event(selectedMessage.value!!)
    }

    fun deleteMessage() {
        _messageToDelete.value = Event(selectedMessage.value!!)
    }

    fun cancelReply() {
        replyMessage.value = null
    }

    companion object {
        const val TAG = "ConversationViewModel"
    }
}
