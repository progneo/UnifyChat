package com.progcorp.unitedmessengers.ui.mailing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.enums.MailingState

@Suppress("UNCHECKED_CAST")
class MailingViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MailingViewModel() as T
    }
}

class MailingViewModel : ViewModel() {
    val conversationsList = App.application.mailingList

    val messageText = MutableLiveData<String?>()

    private val _sendMessageEvent = MutableLiveData<Event<Unit>>()
    val sendMessageEvent: LiveData<Event<Unit>> = _sendMessageEvent

    private val _confirmConversationsListEvent = MutableLiveData<Event<Unit>>()
    val confirmConversationsListEvent: LiveData<Event<Unit>> = _confirmConversationsListEvent

    private val _currentState = MutableLiveData(MailingState.Conversations)
    val currentState: LiveData<MailingState> = _currentState

    val isListEmpty: Boolean
        get() {
            var isEmpty = true
            conversationsList.value?.let {
                if (it.size > 0) {
                    isEmpty = false
                }
            }
            return isEmpty
        }

    fun setState(state: MailingState) {
        _currentState.value = state
    }

    fun removeConversationPressed(conversation: Conversation) {
        conversationsList.value?.let { list ->
            val index = list.indexOf(conversation)
            list.remove(conversation)
        }
    }

    fun confirmConversationsListPressed() {
        _confirmConversationsListEvent.value = Event(Unit)
        setState(MailingState.Message)
    }

    fun sendPressed() {
        _sendMessageEvent.value = Event(Unit)
    }
}