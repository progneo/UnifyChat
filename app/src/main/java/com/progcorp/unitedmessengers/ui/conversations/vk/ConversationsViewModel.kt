package com.progcorp.unitedmessengers.ui.conversations.vk

import android.util.Log
import androidx.lifecycle.*
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.db.vk.VKConversations
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.ui.DefaultViewModel
import com.progcorp.unitedmessengers.util.addNewItem
import com.progcorp.unitedmessengers.util.updateItemAt
import com.vk.api.sdk.VK

class ConversationViewModelFactory() :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConversationsViewModel() as T
    }
}

enum class LayoutState {
    LOGGED_ID, NEED_TO_LOGIN
}

class ConversationsViewModel() : DefaultViewModel(), VKConversations.OnConversationsFetched {

    private val _conversations: VKConversations = VKConversations(this)

    private val _loginEvent = MutableLiveData<Event<Unit>>()

    private val _updatedConversation = MutableLiveData<Conversation>()
    private val _selectedConversation = MutableLiveData<Event<Conversation>>()
    private val _loginState = MutableLiveData<Boolean>()

    val loginEvent: LiveData<Event<Unit>> = _loginEvent

    var selectedConversation: LiveData<Event<Conversation>> = _selectedConversation
    val conversationsList = MediatorLiveData<MutableList<Conversation>>()
    val layoutState = MediatorLiveData<LayoutState>()

    init {
        conversationsList.addSource(_updatedConversation) { newConversation ->
            val conversation = conversationsList.value?.find { it.id == newConversation.id }
            if (conversation == null) {
                conversationsList.addNewItem(newConversation)
            }
            else {
                conversationsList.updateItemAt(newConversation, conversationsList.value!!.indexOf(conversation))
            }
        }
        _loginState.value = VK.isLoggedIn()
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
        loadAndObserveConversations()
    }

    private fun loadAndObserveConversations() {
        Log.i(TAG, "Loading conversations")
        _conversations.getConversations(0)
    }

    override fun showConversations(chats: ArrayList<Conversation>) {
        Log.i(TAG, "Got conversations: " + chats.size)
        for (conversation in chats) {
            _updatedConversation.value = conversation
        }
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