package com.progcorp.unitedmessengers.ui.conversations.telegram

import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.enums.TelegramAuthStatus
import com.progcorp.unitedmessengers.interfaces.IConversationsViewModel

class TelegramConversationsViewModelFactory :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TelegramConversationsViewModel() as T
    }
}

class TelegramConversationsViewModel : ViewModel(), IConversationsViewModel {

    private val _client = App.application.tgClient

    private val _loginEvent = MutableLiveData<Event<Unit>>()
    val loginEvent: LiveData<Event<Unit>> = _loginEvent

    private val _toTopPressed = MutableLiveData<Event<Unit>>()
    val toTopPressed: LiveData<Event<Unit>> = _toTopPressed

    private val _notifyItemInsertedEvent = MutableLiveData<Event<Int>>()
    val notifyItemInsertedEvent: LiveData<Event<Int>> = _notifyItemInsertedEvent

    private val _notifyItemChangedEvent = MutableLiveData<Event<Int>>()
    val notifyItemChangedEvent: LiveData<Event<Int>> = _notifyItemChangedEvent

    private val _notifyItemMovedEvent = MutableLiveData<Event<Pair<Int, Int>>>()
    val notifyItemMovedEvent: LiveData<Event<Pair<Int, Int>>> = _notifyItemMovedEvent

    private val _notifyItemRangeChangedEvent = MutableLiveData<Event<Pair<Int, Int>>>()
    val notifyItemRangeChangedEvent: LiveData<Event<Pair<Int, Int>>> = _notifyItemRangeChangedEvent

    private val _selectedConversation = MutableLiveData<Event<Conversation>>()
    var selectedConversation: LiveData<Event<Conversation>> = _selectedConversation

    override val conversationsList = _client.conversationsList
    val user: LiveData<User?> = _client.user

    init {
        _client.conversationsViewModel = this
    }

    private fun notifyItemInserted(position: Int) {
        _notifyItemInsertedEvent.value = Event(position)
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

    fun goToLoginPressed() {
        if (_client.authState.value != TelegramAuthStatus.AUTHENTICATED) {
            _loginEvent.value = Event(Unit)
        }
    }

    fun addNewChat(index: Int) {
        notifyItemInserted(index)
    }

    fun updateOnline(index: Int) {
        notifyItemChanged(index)
    }

    fun updateLastMessage(previousIndex: Int, newIndex: Int) {
        if (previousIndex == newIndex) {
            notifyItemChanged(newIndex)
        }
        else {
            notifyItemMoved(Pair(previousIndex, newIndex))
        }
    }

    fun updateNewMessage(previousIndex: Int, newIndex: Int) {
        if (previousIndex == newIndex) {
            notifyItemChanged(newIndex)
        }
        else {
            notifyItemMoved(Pair(previousIndex, newIndex))
        }
    }

    fun updateReadInbox(index: Int) {
        notifyItemChanged(index)
    }

    fun goToTopPressed() {
        _toTopPressed.value = Event(Unit)
    }

    override fun selectConversationPressed(conversation: Conversation) {
        _selectedConversation.value = Event(conversation)
    }

    companion object {
        const val TAG = "ConversationsViewModel"
    }
}