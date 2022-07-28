package com.progcorp.unitedmessengers.ui.mailing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.data.model.MessageText
import com.progcorp.unitedmessengers.enums.MailingState
import com.progcorp.unitedmessengers.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

@Suppress("UNCHECKED_CAST")
class MailingViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MailingViewModel() as T
    }
}

class MailingViewModel : ViewModel() {
    private val _tgClient = App.application.tgClient
    private val _vkClient = App.application.vkClient

    private val _tgRepository = _tgClient.repository
    private val _vkRepository = _vkClient.repository

    val conversationsList = App.application.mailingList

    val messageText = MutableLiveData<String?>()

    private val _sendMessageEvent = MutableLiveData<Event<Unit>>()
    val sendMessageEvent: LiveData<Event<Unit>> = _sendMessageEvent

    private val _confirmConversationsListEvent = MutableLiveData<Event<Unit>>()
    val confirmConversationsListEvent: LiveData<Event<Unit>> = _confirmConversationsListEvent

    private val _currentState = MutableLiveData(MailingState.Conversations)
    val currentState: LiveData<MailingState> = _currentState

    private val _notifyItemRemovedEvent = MutableLiveData<Event<Int>>()
    val notifyItemRemovedEvent: LiveData<Event<Int>> = _notifyItemRemovedEvent

    private val _isListEmpty = MutableLiveData<Boolean>()
    val isListEmpty: LiveData<Boolean> = _isListEmpty

    init { updateListEmptyState() }

    private fun updateListEmptyState() {
        var isEmpty = true
        conversationsList.value?.let {
            if (it.size > 0) {
                isEmpty = false
            }
        }
        _isListEmpty.value = isEmpty
    }

    fun setState(state: MailingState) {
        _currentState.value = state
    }

    fun startMailing() {
        val message = MessageText(messageText.value!!)
        messageText.value = ""
        conversationsList.value?.clear()
        App.application.startMailing(message)
    }

    fun removeConversationPressed(conversation: Conversation) {
        conversationsList.value?.let { list ->
            val index = list.indexOf(conversation)
            list.remove(conversation)
            _notifyItemRemovedEvent.value = Event(index)
            updateListEmptyState()
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