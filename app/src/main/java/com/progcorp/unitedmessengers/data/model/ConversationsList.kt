package com.progcorp.unitedmessengers.data.model

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.db.Conversations
import com.progcorp.unitedmessengers.util.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi

class ConversationsList : Conversations.OnConversationsFetched {

    private val _scope = MainScope()

    private val _conversations: Conversations = Conversations(this)

    private val _newConversation = MutableLiveData<Conversation>()
    private val _updatedConversation = MutableLiveData<Conversation>()

    val conversationsList = MediatorLiveData<MutableList<Conversation>>()

    init {
        conversationsList.addSource(_updatedConversation) { newConversation ->
            val conversation = conversationsList.value?.find {
                it.id == newConversation.id
            }
            if (conversation == null) {
                conversationsList.addNewItem(newConversation)
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
        if (App.application.tgClient.authState.value == Authentication.AUTHENTICATED) {
            loadConversations()
        }
    }

    private fun loadConversations() {
        _scope.launch {
            _conversations.tgGetConversations(false)
        }
    }

    override fun showConversations(chats: ArrayList<Conversation>, isNew: Boolean) {
        _scope.launch {
            for (conversation in chats) {
                _newConversation.value = conversation
            }
            if (conversationsList.value != null) {
                conversationsList.value!!.sortByDescending { it.date }
            }
        }
    }

    fun addNewChat(update: TdApi.UpdateNewChat) {
        _scope.launch {
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
        _scope.launch {
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
        _scope.launch {
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
        _scope.launch {
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
        _scope.launch {
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
}