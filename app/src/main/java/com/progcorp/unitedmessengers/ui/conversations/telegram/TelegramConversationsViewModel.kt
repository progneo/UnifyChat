package com.progcorp.unitedmessengers.ui.conversations.telegram

import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.User
import com.progcorp.unitedmessengers.enums.TelegramAuthStatus
import com.progcorp.unitedmessengers.interfaces.IConversationsViewModel
import com.progcorp.unitedmessengers.util.addFrontItem
import com.progcorp.unitedmessengers.util.removeItem
import com.progcorp.unitedmessengers.util.updateItemAt
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import org.drinkless.td.libcore.telegram.TdApi

class TelegramConversationsViewModelFactory :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TelegramConversationsViewModel() as T
    }
}

class TelegramConversationsViewModel : ViewModel(), IConversationsViewModel {

    private val _client = App.application.tgClient
    private val _repository = App.application.tgRepository

    private val _scope: CoroutineScope = viewModelScope

    private val _loginEvent = MutableLiveData<Event<Unit>>()

    private val _observableConversation = MutableLiveData<Conversation>()
    private val _selectedConversation = MutableLiveData<Event<Conversation>>()
    private val _loginState = MutableLiveData<Boolean>()
    private val _user = MutableLiveData<User?>()

    val loginEvent: LiveData<Event<Unit>> = _loginEvent

    var selectedConversation: LiveData<Event<Conversation>> = _selectedConversation
    val conversationsList = MediatorLiveData<MutableList<Conversation>>()

    val conversations: Flow<List<Conversation>> = flowOf()

    val loginState: LiveData<Boolean> = _loginState
    val user: LiveData<User?> = _user

    init {
        conversationsList.addSource(_observableConversation) { newConversation ->
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
        }
        _loginState.value = when (_client.authState.value) {
            TelegramAuthStatus.AUTHENTICATED -> true
            else -> false
        }
        if (_loginState.value == true) {
            refreshConversations()
        }
        _client.conversationsViewModel = this
    }

    fun refreshConversations() {
        conversationsList.value = mutableListOf()
        loadConversations()
    }

    fun loadConversations() {
        MainScope().launch {
            val conv = _repository.getConversations()
            conversations.collect { conv.collect() }
        }
        conversationsList.value?.sortByDescending { it.lastMessage?.timeStamp }
    }

    fun goToLoginPressed() {
        _loginEvent.value = Event(Unit)
    }

    fun addNewChat(update: TdApi.UpdateNewChat) {
        MainScope().launch {
            val conversation = conversationsList.value?.find {
                it.id == update.chat.id
            }
            if (conversation == null) {
                val chat = async { Conversation.tgParse(update.chat) }
                (chat.await())?.let { _observableConversation.value = it }
            }
        }
    }

    fun updateOnline(update: TdApi.UpdateUserStatus) {
        MainScope().launch {
            conversationsList.value?.find {
                it.companion is User && it.companion.id == update.userId
            }?.tgParseOnlineStatus(update)
        }
    }

    fun updateLastMessage(update: TdApi.UpdateChatLastMessage) {
        MainScope().launch {
            conversationsList.value?.find {
                it.id == update.chatId
            }?.tgParseLastMessage(update)
        }
    }

    fun updateNewMessage(update: TdApi.UpdateNewMessage) {
        MainScope().launch {
            conversationsList.value?.find {
                it.id == update.message.chatId
            }?.tgParseNewMessage(update)
        }
    }

    fun updateReadInbox(update: TdApi.UpdateChatReadInbox) {
        MainScope().launch {
            conversationsList.value?.find {
                it.id == update.chatId
            }?.unreadCount = update.unreadCount
        }
    }

    override fun selectConversationPressed(conversation: Conversation) {
        _selectedConversation.value = Event(conversation)
    }

    companion object {
        const val TAG = "ConversationsViewModel"
    }
}