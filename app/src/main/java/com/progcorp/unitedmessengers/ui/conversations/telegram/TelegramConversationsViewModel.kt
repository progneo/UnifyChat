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
import com.progcorp.unitedmessengers.ui.conversations.ConversationsListAdapter
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

    private val _notifyItemInsertedEvent = MutableLiveData<Event<Int>>()
    private val _notifyItemChangedEvent = MutableLiveData<Event<Int>>()
    private val _notifyItemMovedEvent = MutableLiveData<Event<Pair<Int, Int>>>()

    private var _observableConversation = MutableLiveData<Conversation>()
    private val _selectedConversation = MutableLiveData<Event<Conversation>>()

    private val _loginState = MutableLiveData<Boolean>()
    private val _loadingState = MutableLiveData<Status>()

    private val _user = MutableLiveData<User?>()

    val loginEvent: LiveData<Event<Unit>> = _loginEvent

    val notifyItemInsertedEvent: LiveData<Event<Int>> = _notifyItemInsertedEvent
    val notifyItemChangedEvent: LiveData<Event<Int>> = _notifyItemChangedEvent
    val notifyItemMovedEvent: LiveData<Event<Pair<Int, Int>>> = _notifyItemMovedEvent

    var selectedConversation: LiveData<Event<Conversation>> = _selectedConversation
    val conversationsList = MediatorLiveData<MutableList<Conversation>>()

    val loginState: LiveData<Boolean> = _loginState
    val user: LiveData<User?> = _user

    init {
        conversationsList.addSource(_observableConversation) { newConversation ->
            val conversation = conversationsList.value?.find {
                it.id == newConversation.id
            }
            if (conversation == null) {
                conversationsList.addFrontItem(newConversation)
                conversationsList.value?.sortByDescending {
                    it.lastMessage?.timeStamp
                }
            }
        }
        _loginState.value = when (_client.authState.value) {
            TelegramAuthStatus.AUTHENTICATED -> true
            else -> false
        }
        if (_loginState.value == true) {
            _user.value = User()
            fetchChats()
            getMe()
        }
        _client.conversationsViewModel = this
    }

    private fun fetchChats() {
        MainScope().launch {
            val data = _repository.getConversations(1000).first()
            for (conversation in data) {
                val chat = Conversation.tgParse(conversation)
                chat?.let { _observableConversation.value = it }
            }
        }
    }

    private fun getMe() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = _repository.getMe().first()
            val user = User.tgParse(data)
            _user.postValue(user)
            Thread.sleep(1000)
            _user.postValue(user)
        }
    }

    private fun notifyItemInserted(position: Int) {
        _notifyItemInsertedEvent.value = Event(position)
    }

    private fun notifyItemChanged(position: Int) {
        _notifyItemChangedEvent.value = Event(position)
    }

    private fun notifyItemMoved(pair: Pair<Int, Int>) {
        _notifyItemMovedEvent.value = Event(pair)
    }

    fun goToLoginPressed() {
        if (_loginState.value == false) {
            _loginEvent.value = Event(Unit)
        }
    }

    fun addNewChat(update: TdApi.UpdateNewChat) {
        viewModelScope.launch {
            val conversation = conversationsList.value?.find {
                it.id == update.chat.id
            }
            if (conversation == null) {
                val chat = Conversation.tgParse(update.chat)
                chat?.let { _observableConversation.value = it }
                conversationsList.value?.sortByDescending {
                    it.lastMessage?.timeStamp
                }
                conversationsList.value?.indexOf(chat)?.let { notifyItemInserted(it) }
            }
        }
    }

    fun updateOnline(update: TdApi.UpdateUserStatus) {
        viewModelScope.launch {
            val item = conversationsList.value?.find {
                it.companion is User && it.companion.id == update.userId
            }
            item?.tgParseOnlineStatus(update)
            conversationsList.value?.indexOf(item)?.let { notifyItemChanged(it) }
        }
    }

    fun updateLastMessage(update: TdApi.UpdateChatLastMessage) {
        viewModelScope.launch {
            val item = conversationsList.value?.find {
                it.id == update.chatId
            }
            if (item != null) {
                val previousIndex = conversationsList.value?.indexOf(item)
                item.tgParseLastMessage(update)
                conversationsList.value?.sortByDescending {
                    it.lastMessage?.timeStamp
                }
                conversationsList.value?.indexOf(item)?.let {
                    notifyItemMoved(Pair(previousIndex!!, it))
                }
            }
        }
    }

    fun updateNewMessage(update: TdApi.UpdateNewMessage) {
        viewModelScope.launch {
            val item = conversationsList.value?.find {
                it.id == update.message.chatId
            }
            if (item != null) {
                val previousIndex = conversationsList.value?.indexOf(item)
                item.tgParseNewMessage(update)
                conversationsList.value?.sortByDescending {
                    it.lastMessage?.timeStamp
                }
                conversationsList.value?.indexOf(item)?.let {
                    notifyItemMoved(Pair(previousIndex!!, it))
                }
            }
        }
    }

    fun updateReadInbox(update: TdApi.UpdateChatReadInbox) {
        viewModelScope.launch {
            val item = conversationsList.value?.find {
                it.id == update.chatId
            }
            item?.unreadCount = update.unreadCount
            conversationsList.value?.indexOf(item)?.let {
                notifyItemChanged(it)
            }
        }
    }

    override fun selectConversationPressed(conversation: Conversation) {
        _selectedConversation.value = Event(conversation)
    }

    companion object {
        const val TAG = "ConversationsViewModel"
    }
}