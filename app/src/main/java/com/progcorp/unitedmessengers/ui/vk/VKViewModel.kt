package com.progcorp.unitedmessengers.ui.vk

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.ApiResult
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.User
import com.progcorp.unitedmessengers.util.addFrontItem
import com.progcorp.unitedmessengers.util.addNewItem
import com.progcorp.unitedmessengers.util.removeItem
import com.progcorp.unitedmessengers.util.updateItemAt
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class VKViewModel : ViewModel() {
    private val repository = App.application.vkRepository
    private val service = App.application.vkAccountService

    private var _handler = Handler()
    private var _conversationsGetter: Runnable = Runnable {  }

    private val _newConversation = MutableLiveData<Conversation>()
    private val _updatedConversation = MutableLiveData<Conversation>()
    private val _loginState = MutableLiveData<Boolean>()

    val conversationsList = MediatorLiveData<MutableList<Conversation>>()

    val loginState: LiveData<Boolean> = _loginState

    init {
        conversationsList.addSource(_updatedConversation) { newConversation ->
            val conversation = conversationsList.value?.find {
                it.id == newConversation.id
            }
            if (conversation == null) {
                conversationsList.addNewItem(newConversation)
            }
            else {
                if (newConversation.companion is User) {
                    if (newConversation.companion.isOnline != (conversation.companion as User).isOnline) {
                        conversationsList.updateItemAt(newConversation, conversationsList.value!!.indexOf(conversation))
                    }
                }
                if (newConversation.lastMessage?.timeStamp != conversation.lastMessage?.timeStamp) {
                    conversationsList.removeItem(conversation)
                    conversationsList.addFrontItem(newConversation)
                }
                else if (newConversation.unreadCount != conversation.unreadCount) {
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
                if (newConversation.companion is User) {
                    if (newConversation.companion.isOnline != (conversation.companion as User).isOnline) {
                        conversationsList.updateItemAt(newConversation, conversationsList.value!!.indexOf(conversation))
                    }
                }
                if (newConversation.lastMessage?.timeStamp != conversation.lastMessage?.timeStamp) {
                    conversationsList.removeItem(conversation)
                    conversationsList.addFrontItem(newConversation)
                }
                else if (newConversation.unreadCount != conversation.unreadCount) {
                    conversationsList.updateItemAt(newConversation, conversationsList.value!!.indexOf(conversation))
                }
            }
        }
        _loginState.value = (service.token != null)
        if (_loginState.value == true) {
            setupConversations()
        }
    }

    private fun startGetter() {
        _conversationsGetter = Runnable {
            loadNewConversations()
            _handler.postDelayed(_conversationsGetter, 5000)
        }
        _handler.postDelayed(_conversationsGetter, 0)
    }

    private fun setupConversations() {
        startGetter()
        loadConversations(0)
    }

    private fun loadConversations(offset: Int) {
        MainScope().launch {
            val conversations = async {repository.getConversations(offset)}
            conversations.await().map { list -> {
                list.forEach {
                    _newConversation.value = it
                }
            }}
        }
    }

    private fun loadNewConversations() {
        MainScope().launch {
            val conversations = async {repository.getConversations(0)}
            conversations.await().map { list -> {
                list.forEach {
                    _newConversation.value = it
                }
            }}
        }
    }

    fun loadMoreConversations() {
        loadConversations(conversationsList.value!!.size)
    }
}