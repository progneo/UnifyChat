package com.progcorp.unitedmessengers.ui.conversations.vk

import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.enums.VKAuthStatus
import com.progcorp.unitedmessengers.interfaces.IConversationsViewModel

class VKConversationsViewModelFactory :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VKConversationsViewModel() as T
    }
}

class VKConversationsViewModel : ViewModel(), IConversationsViewModel {

    private val _client = App.application.vkClient

    private val _loginEvent = MutableLiveData<Event<Unit>>()
    val loginEvent: LiveData<Event<Unit>> = _loginEvent

    private val _toTopPressed = MutableLiveData<Event<Unit>>()
    val toTopPressed: LiveData<Event<Unit>> = _toTopPressed

    private val _selectedConversation = MutableLiveData<Event<Conversation>>()
    var selectedConversation: LiveData<Event<Conversation>> = _selectedConversation

    val user: LiveData<User?> = _client.user
    override val conversationsList = _client.conversationsList

    fun loadMoreConversations() {
        _client.loadConversations(conversationsList.value!!.size, false)
    }

    fun goToLoginPressed() {
        if (_client.authStatus.value != VKAuthStatus.SUCCESS) {
            _loginEvent.value = Event(Unit)
        }
    }

    fun goToTopPressed() {
        _toTopPressed.value = Event(Unit)
    }

    override fun selectConversationPressed(conversation: Conversation) {
        _selectedConversation.value = Event(conversation)
    }

    companion object {
        const val TAG = "VKConversationsViewModel"
    }
}