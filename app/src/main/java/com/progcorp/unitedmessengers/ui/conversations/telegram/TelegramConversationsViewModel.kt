package com.progcorp.unitedmessengers.ui.conversations.telegram

import android.os.Handler
import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.db.Conversations
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.interfaces.IConversationsViewModel
import com.progcorp.unitedmessengers.ui.DefaultViewModel
import com.progcorp.unitedmessengers.util.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class TelegramConversationsViewModelFactory :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TelegramConversationsViewModel() as T
    }
}

enum class LayoutState {
    LOGGED_ID, NEED_TO_LOGIN
}

class TelegramConversationsViewModel : DefaultViewModel(), Conversations.OnConversationsFetched, IConversationsViewModel {

    private val _scope = MainScope()

    private var _handler = Handler()
    private var _conversationsGetter: Runnable = Runnable {  }

    private val _conversations: Conversations = Conversations(this)

    private val _loginEvent = MutableLiveData<Event<Unit>>()

    private val _newConversation = MutableLiveData<Conversation>()
    private val _updatedConversation = MutableLiveData<Conversation>()
    private val _selectedConversation = MutableLiveData<Event<Conversation>>()
    private val _loginState = MutableLiveData<Boolean>()

    val loginEvent: LiveData<Event<Unit>> = _loginEvent

    var selectedConversation: LiveData<Event<Conversation>> = _selectedConversation
    val conversationsList = MediatorLiveData<MutableList<Conversation>>()
    val layoutState = MediatorLiveData<LayoutState>()

    init {
        conversationsList.addSource(_updatedConversation) { newConversation ->
            val conversation = conversationsList.value?.find {
                it.id == newConversation.id
            }
            if (conversation == null) {
                conversationsList.addNewItem(newConversation)
            }
            else {
                if (newConversation.date != conversation.date) {
                    conversationsList.removeItem(conversation)
                    conversationsList.addFrontItem(newConversation)
                }
                else if (newConversation.unread_count != conversation.unread_count ||
                    newConversation.is_online != conversation.is_online) {
                    conversationsList.updateItemAt(newConversation, conversationsList.value!!.indexOf(conversation))
                }
            }
        }
        conversationsList.addSource(_newConversation) { newConversation ->
            val conversation = conversationsList.value?.find {
                it.id == newConversation.id
            }
            if (conversation == null) {
                conversationsList.addFrontItem(newConversation)
            }
            else {
                if (newConversation.date != conversation.date) {
                    conversationsList.removeItem(conversation)
                    conversationsList.addFrontItem(newConversation)
                }
                else if (newConversation.unread_count != conversation.unread_count ||
                        newConversation.is_online != conversation.is_online) {
                    conversationsList.updateItemAt(newConversation, conversationsList.value!!.indexOf(conversation))
                }
            }
        }
        _loginState.value = when (App.application.tgClient.authState.value) {
            Authentication.AUTHENTICATED -> true
            else -> false
        }
        layoutState.addSource(_loginState) { updateLayoutState(it) }
        if (_loginState.value == true) {
            setupConversations()
        }
        App.application.tgClient.conversationsViewModel = this
    }

    private fun updateLayoutState(loginState: Boolean?) {
        if (loginState != null) {
            layoutState.value = when (_loginState.value) {
                true -> LayoutState.LOGGED_ID
                else -> LayoutState.NEED_TO_LOGIN
            }
        }
    }

    private fun setupConversations() {
        startGetter()
        loadConversations()
    }

    private fun loadConversations() {
        _scope.launch {
            _conversations.tgGetConversations(false)
        }
    }

    private fun loadNewConversations() {
        _scope.launch {
            _conversations.tgGetConversations(true)
        }
    }

    private fun startGetter() {
        _conversationsGetter = Runnable {
            loadNewConversations()
            _handler.postDelayed(_conversationsGetter, 5000)
        }
        _handler.postDelayed(_conversationsGetter, 0)
    }

    override fun showConversations(chats: ArrayList<Conversation>, isNew: Boolean) {
        if (!isNew) {
            for (conversation in chats) {
                _updatedConversation.value = conversation
            }
        }
        else {
            for (conversation in chats) {
                _newConversation.value = conversation
            }
        }
        if (conversationsList.value != null) {
            conversationsList.value!!.sortByDescending { it.date }
        }
    }

    fun loadMoreConversations() {
        loadConversations()
    }

    fun goToLoginPressed() {
        _loginEvent.value = Event(Unit)
    }

    override fun selectConversationPressed(conversation: Conversation) {
        _selectedConversation.value = Event(conversation)
    }

    companion object {
        const val TAG = "ConversationsViewModel"
    }
}