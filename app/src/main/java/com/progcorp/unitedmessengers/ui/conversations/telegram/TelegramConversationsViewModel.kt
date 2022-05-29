package com.progcorp.unitedmessengers.ui.conversations.telegram

import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.Resource
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.User
import com.progcorp.unitedmessengers.enums.Status
import com.progcorp.unitedmessengers.enums.TelegramAuthStatus
import com.progcorp.unitedmessengers.interfaces.IConversationsViewModel
import com.progcorp.unitedmessengers.util.addFrontItem
import com.progcorp.unitedmessengers.util.removeItem
import com.progcorp.unitedmessengers.util.updateItemAt
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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

    private val _loginEvent = MutableLiveData<Event<Unit>>()

    private val _conversations = MutableStateFlow<Resource<List<Conversation>>>(Resource.loading(null))

    private var _observableConversation = MutableLiveData<Conversation>()
    private val _selectedConversation = MutableLiveData<Event<Conversation>>()

    private val _loginState = MutableLiveData<Boolean>()
    private val _loadingState = MutableLiveData<Status>()

    private val _user = MutableLiveData<User?>()

    val loginEvent: LiveData<Event<Unit>> = _loginEvent

    var selectedConversation: LiveData<Event<Conversation>> = _selectedConversation
    val conversationsList = MediatorLiveData<MutableList<Conversation>>()

    val loadingState: LiveData<Status> = _loadingState
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
            viewModelScope.launch {
                val data = _repository.getConversations().first()
                when(data.status) {
                    Status.SUCCESS -> {
                        _loadingState.value = Status.SUCCESS
                        for (conversation in data.data!!) {
                            _observableConversation.value = conversation
                        }
                    }
                    Status.LOADING -> {
                        _loadingState.value = Status.LOADING
                    }
                    Status.ERROR -> {
                        _loadingState.value = Status.ERROR
                    }
                }
            }
        }
        _client.conversationsViewModel = this
    }

    fun goToLoginPressed() {
        _loginEvent.value = Event(Unit)
    }

    fun addNewChat(update: TdApi.UpdateNewChat) {
        viewModelScope.launch {
            val conversation = conversationsList.value?.find {
                it.id == update.chat.id
            }
            if (conversation == null) {
                val chat = Conversation.tgParse(update.chat)
                chat?.let { _observableConversation.value = it }
                //val chat = _repository.getConversation(update.chat.id).first()
                //if (chat.status == Status.SUCCESS) {
                //    chat.data?.let { _observableConversation.value = it }
                //}
            }
        }
    }

    fun updateOnline(update: TdApi.UpdateUserStatus) {
        viewModelScope.launch {
            conversationsList.value?.find {
                it.companion is User && it.companion.id == update.userId
            }?.tgParseOnlineStatus(update)
        }
    }

    fun updateLastMessage(update: TdApi.UpdateChatLastMessage) {
        viewModelScope.launch {
            conversationsList.value?.find {
                it.id == update.chatId
            }?.tgParseLastMessage(update)
        }
    }

    fun updateNewMessage(update: TdApi.UpdateNewMessage) {
        viewModelScope.launch {
            conversationsList.value?.find {
                it.id == update.message.chatId
            }?.tgParseNewMessage(update)
        }
    }

    fun updateReadInbox(update: TdApi.UpdateChatReadInbox) {
        viewModelScope.launch {
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