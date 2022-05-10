package com.progcorp.unitedmessengers.ui.conversations.telegram

import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.interfaces.IConversationsViewModel
import com.progcorp.unitedmessengers.ui.DefaultViewModel
import com.progcorp.unitedmessengers.util.*

class TelegramConversationsViewModelFactory :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TelegramConversationsViewModel() as T
    }
}

enum class LayoutState {
    LOGGED_ID, NEED_TO_LOGIN
}

class TelegramConversationsViewModel : DefaultViewModel(), IConversationsViewModel {

    private val _loginEvent = MutableLiveData<Event<Unit>>()

    private val _selectedConversation = MutableLiveData<Event<Conversation>>()
    private val _loginState = MutableLiveData<Boolean>()

    val loginEvent: LiveData<Event<Unit>> = _loginEvent

    var selectedConversation: LiveData<Event<Conversation>> = _selectedConversation

    val layoutState = MediatorLiveData<LayoutState>()

    var conversationsList = App.application.tgConversationsList.conversationsList

    init {
        _loginState.value = when (App.application.tgClient.authState.value) {
            Authentication.AUTHENTICATED -> true
            else -> false
        }
        layoutState.addSource(_loginState) { updateLayoutState(it) }
    }

    private fun updateLayoutState(loginState: Boolean?) {
        if (loginState != null) {
            layoutState.value = when (_loginState.value) {
                true -> LayoutState.LOGGED_ID
                else -> LayoutState.NEED_TO_LOGIN
            }
        }
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