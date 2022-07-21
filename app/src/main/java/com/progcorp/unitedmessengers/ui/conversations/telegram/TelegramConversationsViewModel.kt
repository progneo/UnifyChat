package com.progcorp.unitedmessengers.ui.conversations.telegram

import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.enums.TelegramAuthStatus
import com.progcorp.unitedmessengers.interfaces.IConversationsViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class TelegramConversationsViewModelFactory :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TelegramConversationsViewModel() as T
    }
}

class TelegramConversationsViewModel : ViewModel(), IConversationsViewModel {

    private val _client = App.application.tgClient
    private val _repository = App.application.tgClient.repository

    private val _loginEvent = MutableLiveData<Event<Unit>>()
    private val _toTopPressed = MutableLiveData<Event<Unit>>()

    private val _notifyItemInsertedEvent = MutableLiveData<Event<Int>>()
    private val _notifyItemChangedEvent = MutableLiveData<Event<Int>>()
    private val _notifyItemMovedEvent = MutableLiveData<Event<Pair<Int, Int>>>()

    private val _selectedConversation = MutableLiveData<Event<Conversation>>()

    private val _loginState = MutableLiveData<Boolean>()

    private val _user = MutableLiveData<User?>()

    val loginEvent: LiveData<Event<Unit>> = _loginEvent
    val toTopPressed: LiveData<Event<Unit>> = _toTopPressed

    val notifyItemInsertedEvent: LiveData<Event<Int>> = _notifyItemInsertedEvent
    val notifyItemChangedEvent: LiveData<Event<Int>> = _notifyItemChangedEvent
    val notifyItemMovedEvent: LiveData<Event<Pair<Int, Int>>> = _notifyItemMovedEvent

    var selectedConversation: LiveData<Event<Conversation>> = _selectedConversation
    override val conversationsList = _client.conversationsList

    val loginState: LiveData<Boolean> = _loginState
    val user: LiveData<User?> = _user

    init {
        _loginState.value = when (_client.authState.value) {
            TelegramAuthStatus.AUTHENTICATED -> true
            else -> false
        }
        if (_loginState.value == true) {
            _user.value = User()
            _client.fetchChats()
            getMe()
        }
        _client.conversationsViewModel = this
    }

    private fun getMe() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = _repository.getMe().first()
            val user = User.tgParse(data)
            _user.postValue(user)
        }
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

    fun goToLoginPressed() {
        if (_loginState.value == false) {
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
        notifyItemMoved(Pair(previousIndex, newIndex))
    }

    fun updateNewMessage(previousIndex: Int, newIndex: Int) {
        notifyItemMoved(Pair(previousIndex, newIndex))
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