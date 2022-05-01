package com.progcorp.unitedmessengers.ui.conversations.vk

import android.os.Handler
import android.util.Log
import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.db.Conversations
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.ui.DefaultViewModel
import com.progcorp.unitedmessengers.util.addFrontItem
import com.progcorp.unitedmessengers.util.addNewItem
import com.progcorp.unitedmessengers.util.removeItem
import com.progcorp.unitedmessengers.util.updateItemAt

class ConversationViewModelFactory() :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConversationsViewModel() as T
    }
}

enum class LayoutState {
    LOGGED_ID, NEED_TO_LOGIN
}

class ConversationsViewModel() : DefaultViewModel(), Conversations.OnConversationsFetched {

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
        _loginState.value = (App.application.vkAccountService.token != null)
        layoutState.addSource(_loginState) { updateLayoutState(it) }
        if (_loginState.value == true) {
            setupConversations()
        }
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
        loadConversations(0)
    }

    private fun loadConversations(offset: Int) {
        _conversations.vkGetConversations(offset, false)
    }

    private fun loadNewConversations() {
        _conversations.vkGetConversations(0, true)
    }

    private fun startGetter() {
        _conversationsGetter = Runnable {
            loadNewConversations()
            _handler.postDelayed(_conversationsGetter, 5000)
        }
        _handler.postDelayed(_conversationsGetter, 5000)
    }

    override fun showConversations(chats: ArrayList<Conversation>, isNew: Boolean) {
        Log.i(TAG, "Got conversations: " + chats.size)
        chats.sortByDescending {
            it.date
        }
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
    }

    fun loadMoreConversations() {
        loadConversations(conversationsList.value!!.size)
    }

    fun goToLoginPressed() {
        _loginEvent.value = Event(Unit)
    }

    fun selectConversationPressed(conversation: Conversation) {
        _selectedConversation.value = Event(conversation)
    }

    companion object {
        const val TAG = "ConversationsViewModel"
    }
}