package com.progcorp.unitedmessengers.ui.conversation

import android.os.Handler
import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.model.*
import com.progcorp.unitedmessengers.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
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

class ConversationViewModel(private val _chat: Conversation) : ViewModel()  {

    //Clients
    private val _tgClient = App.application.tgClient
    private val _vkClient = App.application.vkClient

    //Handler for vk (will be removed, when long polling will be finished
    private var _handler = Handler()
    private var _messagesGetter: Runnable = Runnable {  }

    //Current data
    private val _conversation: MutableLiveData<Conversation> = MutableLiveData()
    val conversation: LiveData<Conversation> = _conversation
    val messagesList = MediatorLiveData<MutableList<Message>>()

    //Observable messages variables
    private val _addedMessage = MutableLiveData<Message>()
    private val _newMessage = MutableLiveData<Message>()

    val replyMessage = MutableLiveData<Message?>()
    val editMessage = MutableLiveData<Message?>()
    val selectedMessage = MutableLiveData<Message?>()

    //Events on clicks in activity
    private val _addAttachmentsPressed = MutableLiveData<Event<Unit>>()
    val addAttachmentPressed: LiveData<Event<Unit>> = _addAttachmentsPressed

    private val _toBottomPressed = MutableLiveData<Event<Unit>>()
    val toBottomPressed: LiveData<Event<Unit>> = _toBottomPressed

    private val _onMessagePressed = MutableLiveData<Event<Message>>()
    val onMessagePressed: LiveData<Event<Message>> = _onMessagePressed

    //Events on clicks in bottom sheet
    private val _messageToReply = MutableLiveData<Event<Message>>()
    val messageToReply: LiveData<Event<Message>> = _messageToReply

    private val _messagesToForward = MutableLiveData<Event<List<Message>>>()
    val messagesToForward: LiveData<Event<List<Message>>> = _messagesToForward

    private val _textToCopy = MutableLiveData<Event<String>>()
    val textToCopy: LiveData<Event<String>> = _textToCopy

    private val _messageToEdit = MutableLiveData<Event<Message>>()
    val messageToEdit: LiveData<Event<Message>> = _messageToEdit

    private val _messageToDelete = MutableLiveData<Event<Message>>()
    val messageToDelete: LiveData<Event<Message>> = _messageToDelete

    //New message text
    val messageText = MutableLiveData<String?>()


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
        _conversation.value = _chat
        startListeners()
    }

    private fun startListeners() {
        when (_conversation.value?.messenger) {
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
            when (_conversation.value?.messenger) {
                Constants.Messenger.VK -> {
                    val data = _vkClient.repository.getMessages(_conversation.value!!, offset, 20).first()
                    for (message in data) {
                        _addedMessage.value = message
                    }
                }
                Constants.Messenger.TG -> {
                    if (messagesList.value != null) {
                        val data = _tgClient.repository.getMessages(
                            _conversation.value!!.id,
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
            when (_conversation.value?.messenger) {
                Constants.Messenger.VK -> {
                    val data = _vkClient.repository.getMessages(_conversation.value!!, 0, 20).first()
                    for (message in data) {
                        _newMessage.value = message
                    }
                }
                Constants.Messenger.TG -> {
                    val data = _tgClient.repository.getMessages(_conversation.value!!.id, 0,20).first()
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
        MainScope().launch {
            when (_conversation.value?.messenger) {
                Constants.Messenger.VK -> {
                    _vkClient.repository.markAsRead(conversation.value!!.id, message).first()
                }
                Constants.Messenger.TG -> {
                    _tgClient.repository.markAsRead(conversation.value!!.id, message).first()
                }
            }
        }
    }

    fun stopListeners() {
        when (_conversation.value?.messenger) {
            Constants.Messenger.VK -> {
                _handler.removeCallbacks(_messagesGetter)
            }
            Constants.Messenger.TG -> {
                _tgClient.conversationViewModel = null
            }
        }
    }

    fun sendMessage() {
        MainScope().launch {
            if (!messageText.value.isNullOrBlank()) {
                val message = Message(
                    id = messagesList.value!!.last().id + 1,
                    timeStamp = Date().time,
                    sender = conversation.value!!.companion,
                    isOutgoing = true,
                    replyToMessage = replyMessage.value,
                    content = MessageText(messageText.value!!)
                )
                messageText.value = null
                replyMessage.value = null
                editMessage.value = null

                when (_conversation.value?.messenger) {
                    Constants.Messenger.VK -> {
                        _newMessage.value = message
                        val data = _vkClient.repository.sendMessage(conversation.value!!.id, message).first()
                        _newMessage.value?.id = data
                    }
                    Constants.Messenger.TG -> {
                        val data = _tgClient.repository.sendMessage(conversation.value!!.id, message).first()
                        _newMessage.value = Message.tgParse(data)
                    }
                }
            }
        }
    }

    fun editMessage() {
        MainScope().launch {
            if (!messageText.value.isNullOrBlank()) {
                val message = editMessage.value!!
                message.content.text = messageText.value!!
                messageText.value = null
                editMessage.value = null

                when (_conversation.value?.messenger) {
                    Constants.Messenger.VK -> {
                        _vkClient.repository.editMessage(_conversation.value!!, message).first()
                    }
                    Constants.Messenger.TG -> {
                        when (message.content) {
                            is MessageText -> {
                                _tgClient.repository.editMessageText(_conversation.value!!.id, message).first()
                            }
                            is MessagePhoto -> {
                                _tgClient.repository.editMessageCaption(_conversation.value!!.id, message).first()
                            }
                            is MessageAnimation -> {
                                _tgClient.repository.editMessageCaption(_conversation.value!!.id, message).first()
                            }
                            is MessageVideo -> {
                                _tgClient.repository.editMessageCaption(_conversation.value!!.id, message).first()
                            }
                            is MessageVoiceNote -> {
                                _tgClient.repository.editMessageCaption(_conversation.value!!.id, message).first()
                            }
                            is MessageDocument -> {
                                _tgClient.repository.editMessageCaption(_conversation.value!!.id, message).first()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    fun deleteMessage(forAll: Boolean) {
        MainScope().launch {
            selectedMessage.value?.let {
                if (it.id == editMessage.value?.id) {
                    editMessage.value = null
                    messageText.value = null
                }
                else if (it.id == replyMessage.value?.id) {
                    replyMessage.value = null
                }
                when (_conversation.value?.messenger) {
                    Constants.Messenger.VK -> {
                        _vkClient.repository.deleteMessages(listOf(it), forAll).first()
                    }
                    Constants.Messenger.TG -> {
                        _tgClient.repository.deleteMessages(conversation.value!!.id, listOf(it), forAll).first()
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
        if (data.userId == conversation.value!!.id) {
            viewModelScope.launch(Dispatchers.Main) {
                _conversation.value?.tgParseOnlineStatus(data)
                _conversation.postValue(_conversation.value?.copy())
            }
        }
    }

    fun newMessage(data: TdApi.UpdateNewMessage) {
        if (data.message.chatId == conversation.value!!.id) {
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

    fun copyTextToClipboard() {
        selectedMessage.value?.let {
            _textToCopy.value = Event(it.content.text)
        }
    }

    fun onForwardMessage() {
        _messagesToForward.value = Event(listOf(selectedMessage.value!!))
    }

    fun onReplyPressed() {
        selectedMessage.value?.let {
            replyMessage.value = it
            if (editMessage.value != null) {
                messageText.value = null
                editMessage.value = null
            }
            _messageToReply.value = Event(it)
        }
    }

    fun onEditMessage() {
        selectedMessage.value?.let {
            editMessage.value = it
            replyMessage.value = null
            messageText.value = it.content.text
            _messageToEdit.value = Event(it)
        }
    }

    fun onDeleteMessage() {
        selectedMessage.value?.let {
            _messageToDelete.value = Event(it)
        }
    }

    fun cancelReply() {
        replyMessage.value = null
    }

    fun cancelEdit() {
        editMessage.value = null
        messageText.value = null
    }

    companion object {
        const val TAG = "ConversationViewModel"
    }
}
