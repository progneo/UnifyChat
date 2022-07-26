package com.progcorp.unitedmessengers.ui.conversation

import androidx.lifecycle.*
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.model.*
import com.progcorp.unitedmessengers.interfaces.IMessagesList
import com.progcorp.unitedmessengers.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

class ConversationViewModelFactory(private val conversation: Conversation) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConversationViewModel(conversation) as T
    }
}

class ConversationViewModel(chat: Conversation) : ViewModel()  {

    //Current data
    private val _conversation: MutableLiveData<Conversation> = MutableLiveData()
    val conversation: LiveData<Conversation> = _conversation

    private val _messagesList: IMessagesList
    val messagesList: MediatorLiveData<MutableList<Message>>

    //Observable messages variables
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

    //Notify
    private val _notifyItemInsertedEvent = MutableLiveData<Event<Int>>()
    val notifyItemInsertedEvent: LiveData<Event<Int>> = _notifyItemInsertedEvent

    private val _notifyItemRemovedEvent = MutableLiveData<Event<Int>>()
    val notifyItemRemovedEvent: LiveData<Event<Int>> = _notifyItemRemovedEvent

    private val _notifyItemChangedEvent = MutableLiveData<Event<Int>>()
    val notifyItemChangedEvent: LiveData<Event<Int>> = _notifyItemChangedEvent

    private val _notifyItemMovedEvent = MutableLiveData<Event<Pair<Int, Int>>>()
    val notifyItemMovedEvent: LiveData<Event<Pair<Int, Int>>> = _notifyItemMovedEvent

    private val _notifyItemRangeChangedEvent = MutableLiveData<Event<Pair<Int, Int>>>()
    val notifyItemRangeChangedEvent: LiveData<Event<Pair<Int, Int>>> = _notifyItemRangeChangedEvent

    //New message text
    val messageText = MutableLiveData<String?>()

    init {
        _conversation.value = chat
        _messagesList = if (chat.messenger == Constants.Messenger.TG) {
            TgMessagesList(chat, this)
        } else {
            VKMessagesList(chat, this)
        }
        messagesList = _messagesList.messagesList
        MainScope().launch(Dispatchers.Main) {
            _messagesList.loadLatestMessages()
        }
    }

    //Notify
    private fun notifyItemInserted(position: Int) {
        _notifyItemInsertedEvent.value = Event(position)
    }

    private fun notifyItemRemoved(position: Int) {
        _notifyItemRemovedEvent.value = Event(position)
    }

    private fun notifyItemChanged(position: Int) {
        _notifyItemChangedEvent.value = Event(position)
    }

    private fun notifyItemMoved(pair: Pair<Int, Int>) {
        _notifyItemMovedEvent.value = Event(pair)
    }

    private fun notifyItemRangeChanged(pair: Pair<Int, Int>) {
        _notifyItemRangeChangedEvent.value = Event(pair)
    }

    fun updateConversation(conversation: Conversation) {
        _conversation.postValue(conversation)
    }

    //Messages
    fun sendMessage() {
        MainScope().launch {
            if (!messageText.value.isNullOrBlank()) {
                val message = Message(
                    id = messagesList.value?.last()?.id?.plus(1) ?: 0,
                    timeStamp = Date().time,
                    sender = conversation.value!!.companion,
                    isOutgoing = true,
                    replyToMessage = replyMessage.value,
                    content = MessageText(messageText.value!!),
                    canBeEdited = true,
                    canBeDeletedForAllUsers = true,
                    canBeDeletedOnlyForSelf = true
                )
                messageText.value = null
                replyMessage.value = null
                editMessage.value = null

                _messagesList.sendMessage(message)
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

                _messagesList.editMessage(message)
            }
        }
    }

    fun deleteMessages(forAll: Boolean) {
        MainScope().launch {
            selectedMessage.value?.let {
                if (it.id == editMessage.value?.id) {
                    editMessage.value = null
                    messageText.value = null
                }
                else if (it.id == replyMessage.value?.id) {
                    replyMessage.value = null
                }

                _messagesList.deleteMessages(listOf(it), forAll)
            }
        }
    }

    fun loadMoreMessages() {
        MainScope().launch {
            messagesList.value?.let {
                if (it.size > 0) {
                    _messagesList.loadMessagesFromId(it.last().id)
                }
            }
        }
    }

    fun messageEdited(index: Int) {
        notifyItemChanged(index)
    }


    fun messageDeleted(index: Int) {
        notifyItemRemoved(index)
    }

    fun copyTextToClipboard() {
        selectedMessage.value?.let {
            _textToCopy.value = Event(it.content.text)
        }
    }

    //Clicks
    fun toBottomPressed() {
        _toBottomPressed.value = Event(Unit)
    }

    fun addAttachmentsPressed() {
        _addAttachmentsPressed.value = Event(Unit)
    }

    fun onMessagePressed(message: Message) {
        selectedMessage.value = message
        _onMessagePressed.value = Event(message)
    }

    //Bottom sheet clicks
    fun onForwardPressed() {
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

    fun onEditPressed() {
        selectedMessage.value?.let {
            editMessage.value = it
            replyMessage.value = null
            messageText.value = it.content.text
            _messageToEdit.value = Event(it)
        }
    }

    fun onDeletePressed() {
        selectedMessage.value?.let {
            _messageToDelete.value = Event(it)
        }
    }

    //Cancels
    fun cancelReply() {
        replyMessage.value = null
    }

    fun cancelEdit() {
        editMessage.value = null
        messageText.value = null
    }

    override fun onCleared() {
        super.onCleared()
         _messagesList.stopHandlers()
    }

    companion object {
        const val TAG = "ConversationViewModel"
    }
}
