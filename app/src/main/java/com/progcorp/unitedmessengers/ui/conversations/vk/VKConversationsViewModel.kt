package com.progcorp.unitedmessengers.ui.conversations.vk

import android.os.Handler
import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.interfaces.IConversationsViewModel
import com.progcorp.unitedmessengers.util.addFrontItem
import com.progcorp.unitedmessengers.util.addNewItem
import com.progcorp.unitedmessengers.util.removeItem
import com.progcorp.unitedmessengers.util.updateItemAt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class VKConversationsViewModelFactory() :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VKConversationsViewModel() as T
    }
}

class VKConversationsViewModel : ViewModel(), IConversationsViewModel {

    private val _repository = App.application.vkClient.repository

    private var _handler = Handler()
    private var _conversationsGetter: Runnable = Runnable {  }

    private val _loginEvent = MutableLiveData<Event<Unit>>()
    private val _toTopPressed = MutableLiveData<Event<Unit>>()

    private val _newConversation = MutableLiveData<Conversation>()
    private val _updatedConversation = MutableLiveData<Conversation>()
    private val _selectedConversation = MutableLiveData<Event<Conversation>>()
    private val _loginState = MutableLiveData<Boolean>()
    private val _user = MutableLiveData<User?>()

    val loginEvent: LiveData<Event<Unit>> = _loginEvent
    val toTopPressed: LiveData<Event<Unit>> = _toTopPressed

    var selectedConversation: LiveData<Event<Conversation>> = _selectedConversation
    val conversationsList = MediatorLiveData<MutableList<Conversation>>()
    val loginState: LiveData<Boolean> = _loginState
    val user: LiveData<User?> = _user

    init {
        conversationsList.addSource(_updatedConversation) { newConversation ->
            val conversation = conversationsList.value?.find {
                it.id == newConversation.id
            }
            if (conversation == null) {
                conversationsList.addNewItem(newConversation)
            }
            else {
                if (newConversation.lastMessage?.timeStamp != conversation.lastMessage?.timeStamp) {
                    conversationsList.removeItem(conversation)
                    conversationsList.addFrontItem(newConversation)
                }
                else if (newConversation.unreadCount != conversation.unreadCount ||
                    newConversation.getLastOnline() != conversation.getLastOnline()) {
                    conversationsList.updateItemAt(newConversation, conversationsList.value!!.indexOf(conversation))
                }
            }
            conversationsList.value?.sortByDescending {
                it.lastMessage?.timeStamp
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
                if (newConversation.lastMessage?.timeStamp != conversation.lastMessage?.timeStamp) {
                    conversationsList.removeItem(conversation)
                    conversationsList.addFrontItem(newConversation)
                }
                else if (newConversation.unreadCount != conversation.unreadCount ||
                    newConversation.getLastOnline() != conversation.getLastOnline()) {
                    conversationsList.updateItemAt(newConversation, conversationsList.value!!.indexOf(conversation))
                }
            }
            conversationsList.value?.sortByDescending {
                it.lastMessage?.timeStamp
            }
        }
        _loginState.value = (App.application.vkClient.token != null)
        if (_loginState.value == true) {
            _user.value = User()
            setupConversations()
            getMe()
        }
    }

    private fun setupConversations() {
        startGetter()
    }

    private fun loadConversations(offset: Int, isNew: Boolean) {
        MainScope().launch {
            val data = _repository.getConversations(offset).first()
            for (conversation in data) {
                if (isNew) {
                    _newConversation.value = conversation
                }
                else {
                    _updatedConversation.value = conversation
                }
            }
        }
        conversationsList.value?.sortByDescending { it.lastMessage?.timeStamp }
    }

    private fun startGetter() {
        _conversationsGetter = Runnable {
            loadConversations(0, true)
            _handler.postDelayed(_conversationsGetter, 5000)
        }
        _handler.postDelayed(_conversationsGetter, 0)
    }

    fun loadMoreConversations() {
        loadConversations(conversationsList.value!!.size, false)
    }

    private fun getMe() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = _repository.getUsers().first()
            _user.postValue(data[0])
        }
    }

    fun goToLoginPressed() {
        if (_loginState.value == false) {
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
        const val TAG = "ConversationsViewModel"
    }
}