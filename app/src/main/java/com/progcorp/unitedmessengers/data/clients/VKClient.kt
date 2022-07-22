package com.progcorp.unitedmessengers.data.clients

import android.content.SharedPreferences
import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.progcorp.unitedmessengers.data.db.VKDataSource
import com.progcorp.unitedmessengers.data.db.VKRepository
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.VKLongPollServer
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.enums.VKAuthStatus
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel
import com.progcorp.unitedmessengers.ui.conversations.vk.VKConversationsViewModel
import com.progcorp.unitedmessengers.util.addFrontItem
import com.progcorp.unitedmessengers.util.addNewItem
import com.progcorp.unitedmessengers.util.removeItem
import com.progcorp.unitedmessengers.util.updateItemAt
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class VKClient (private val _sharedPreference: SharedPreferences) {
    private var _dataSource: VKDataSource = VKDataSource(this)
    var repository: VKRepository = VKRepository(_dataSource)

    private val _authStatus = MutableLiveData<VKAuthStatus>()
    val authStatus: LiveData<VKAuthStatus> = _authStatus

    private var _handler = Handler()
    private var _conversationsGetter: Runnable = Runnable {  }

    var lpServer: VKLongPollServer? = null
    var lpRetrofit: Retrofit? = null

    val conversationsList = MediatorLiveData<MutableList<Conversation>>()

    var conversationsViewModel: VKConversationsViewModel? = null
    var conversationViewModel: ConversationViewModel? = null

    var user = MutableLiveData<User?>()

    init {
        _authStatus.value = if (token != null) VKAuthStatus.SUCCESS else VKAuthStatus.AUTH
        Log.d("VKClient", "authStatus: ${authStatus.value}")
        setAuth(_authStatus.value!!)
    }

    private fun initLongPoll() {
        MainScope().launch {
            lpServer = repository.getLongPollServer().first()
            lpRetrofit = Retrofit.Builder()
                .baseUrl("https://im.vk.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
        }
    }

    private fun getUser() {
        MainScope().launch {
            val data = repository.getUsers().first()
            user.postValue(data[0])
        }
    }

    private fun setAuth(auth: VKAuthStatus) {
        if (auth == VKAuthStatus.SUCCESS) {
            getUser()
            startGetter()
        }
    }

    fun updateLpServer(ts: Long) {
        lpServer!!.ts = ts
    }

    fun loadConversations(offset: Int, isNew: Boolean) {
        MainScope().launch {
            val data = repository.getConversations(offset).first()
            for (conversation in data) {
                if (isNew) {
                    val existing = conversationsList.value?.find {
                        it.id == conversation.id
                    }
                    if (existing == null) {
                        conversationsList.addFrontItem(conversation)
                    }
                    else {
                        if (conversation.lastMessage?.timeStamp != existing.lastMessage?.timeStamp) {
                            conversationsList.removeItem(existing)
                            conversationsList.addFrontItem(conversation)
                        }
                        else if (conversation.unreadCount != existing.unreadCount ||
                            conversation.getLastOnline() != existing.getLastOnline()) {
                            conversationsList.updateItemAt(conversation, conversationsList.value!!.indexOf(existing))
                        }
                    }
                    conversationsList.value?.sortByDescending {
                        it.lastMessage?.timeStamp
                    }
                }
                else {
                    val existing = conversationsList.value?.find {
                        it.id == conversation.id
                    }
                    if (existing == null) {
                        conversationsList.addNewItem(conversation)
                    }
                    else {
                        if (conversation.lastMessage?.timeStamp != existing.lastMessage?.timeStamp) {
                            conversationsList.removeItem(existing)
                            conversationsList.addFrontItem(conversation)
                        }
                        else if (conversation.unreadCount != existing.unreadCount ||
                            conversation.getLastOnline() != existing.getLastOnline()) {
                            conversationsList.updateItemAt(conversation, conversationsList.value!!.indexOf(existing))
                        }
                    }
                    conversationsList.value?.sortByDescending {
                        it.lastMessage?.timeStamp
                    }
                }
            }
        }
        conversationsList.value?.sortByDescending { it.lastMessage?.timeStamp }
    }

    private fun startGetter() {
        _conversationsGetter = Runnable {
            loadConversations(0, true)
            _handler.postDelayed(_conversationsGetter, 5000)
            Log.d("VKClient", "User: ${user.value}")
            Log.d("VKClient", "ConversationsList: ${conversationsList.value}")
        }
        _handler.postDelayed(_conversationsGetter, 0)
    }

    var token: String?
        get() {
            return _sharedPreference.getString(TOKEN, null)
        }
        set(value) {
            with(_sharedPreference.edit()) {
                if (value == null) {
                    remove(TOKEN)
                    setAuth(VKAuthStatus.AUTH)
                }
                else {
                    putString(TOKEN, value)
                    setAuth(VKAuthStatus.SUCCESS)
                }
                apply()
            }

        }

    var userId: String?
        get() {
            return _sharedPreference.getString(USER_ID, null)
        }
        set(value) {
            with(_sharedPreference.edit()) {
                if (value == null) {
                    remove(USER_ID)
                }
                else {
                    putString(USER_ID, value)
                }
                apply()
            }
        }

    companion object {
        const val SCOPE = "1073737727"
        const val TOKEN = "token"
        const val USER_ID = "userId"
    }
}