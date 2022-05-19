package com.progcorp.unitedmessengers.ui.conversations.telegram

import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.interfaces.IConversationsViewModel
import com.progcorp.unitedmessengers.util.*
import com.progcorp.unitedmessengers.enums.TelegramAuthStatus
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi

class TelegramConversationsViewModelFactory :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TelegramConversationsViewModel() as T
    }
}

enum class LayoutState {
    LOGGED_ID, NEED_TO_LOGIN
}

class TelegramConversationsViewModel : ViewModel(), IConversationsViewModel {

    private val _scope = MainScope()

    private val _conversations: Conversations = Conversations(this)

    private val _loginEvent = MutableLiveData<Event<Unit>>()

    private val _newConversation = MutableLiveData<Conversation>()
    private val _selectedConversation = MutableLiveData<Event<Conversation>>()
    private val _loginState = MutableLiveData<Boolean>()

    val loginEvent: LiveData<Event<Unit>> = _loginEvent

    var selectedConversation: LiveData<Event<Conversation>> = _selectedConversation
    val conversationsList = MediatorLiveData<MutableList<Conversation>>()
    val layoutState = MediatorLiveData<LayoutState>()

    init {
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
        _loginState.value = when (App.application.tgClient.authState.value) {
            TelegramAuthStatus.AUTHENTICATED -> true
            else -> false
        }
        layoutState.addSource(_loginState) { updateLayoutState(it) }
        if (_loginState.value == true) {
            loadConversations()
        }
        App.application.tgClient.conversationsViewModel = this
    }

    private fun updateLayoutState(loginState: Boolean?) {
        if (loginState != null) {
            layoutState.value = when (_loginState.value) {
                true -> LayoutState.LOGGED_ID
                else -> LayoutState.NEED_TO_LOGIN
            }
        }
    }

    fun refreshConversations() {
        conversationsList.value = mutableListOf<Conversation>()
        _scope.launch {
            _conversations.tgGetConversations()
        }
    }

    fun loadConversations() {
        _scope.launch {
            _conversations.tgGetConversations()
        }
    }

    override fun showConversations(chats: ArrayList<Conversation>, isNew: Boolean) {
        for (conversation in chats) {
            _newConversation.value = conversation
        }
        if (conversationsList.value != null) {
            conversationsList.value!!.sortByDescending { it.date }
        }
        _scope.launch {
            loadChatsImages()
        }
    }

    private suspend fun loadChatsImages() {
        if (conversationsList.value != null) {
            for (conversation in conversationsList.value!!) {
                if (conversation.data != null) {
                    _scope.launch {
                        val result = async {
                            App.application.tgClient.downloadableFile(conversation.data as TdApi.File).first()
                        }
                        launch {
                            val photo = result.await()
                            if (photo != null) {
                                conversation.photo = photo
                            }
                        }
                    }
                }
            }
        }
    }

    fun goToLoginPressed() {
        _loginEvent.value = Event(Unit)
    }

    fun addNewChat(update: TdApi.UpdateNewChat) {
        MainScope().launch {
            if (conversationsList.value != null) {
                val conversation = conversationsList.value!!.find {
                    it.user_id == update.chat.id
                }
                if (conversation == null) {
                    val newConversation = Conversation.tgParse(update.chat)
                    if (newConversation != null) {
                        _newConversation.value = newConversation!!
                        if (update.chat.photo != null) {
                            val photo =
                                App.application.tgClient.downloadableFile(update.chat.photo!!.small)
                                    .first()
                            if (photo != null) {
                                newConversation.photo = photo
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateOnline(update: TdApi.UpdateUserStatus) {
        MainScope().launch {
            if (conversationsList.value != null) {
                val conversation = conversationsList.value!!.find {
                    it.user_id == update.userId
                }?.copy()
                if (conversation != null) {
                    Conversation.tgParseOnlineStatus(conversation, update)
                    _newConversation.value = conversation!!
                }
            }
        }
    }

    fun updateLastMessage(update: TdApi.UpdateChatLastMessage) {
        MainScope().launch {
            if (conversationsList.value != null) {
                val conversation = conversationsList.value!!.find {
                    it.id == update.chatId
                }?.copy()
                if (conversation != null) {
                    Conversation.tgParseLastMessage(conversation, update)
                    _newConversation.value = conversation!!
                }
            }
        }
    }

    fun updateNewMessage(update: TdApi.UpdateNewMessage) {
        MainScope().launch {
            if (conversationsList.value != null) {
                val conversation = conversationsList.value!!.find {
                    it.id == update.message.chatId
                }?.copy()
                if (conversation != null) {
                    Conversation.tgParseNewMessage(conversation, update)
                    _newConversation.value = conversation!!
                }
            }
        }
    }

    fun updateReadInbox(update: TdApi.UpdateChatReadInbox) {
        MainScope().launch {
            if (conversationsList.value != null) {
                val conversation = conversationsList.value!!.find {
                    it.id == update.chatId
                }?.copy()
                if (conversation != null) {
                    conversation.unread_count = update.unreadCount
                    _newConversation.value = conversation!!
                }
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