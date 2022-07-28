package com.progcorp.unitedmessengers.data.clients

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.progcorp.unitedmessengers.data.db.VKDataSource
import com.progcorp.unitedmessengers.data.db.VKRepository
import com.progcorp.unitedmessengers.data.model.*
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.enums.VKAuthStatus
import com.progcorp.unitedmessengers.ui.conversations.vk.VKConversationsViewModel
import com.progcorp.unitedmessengers.util.addFrontItem
import com.progcorp.unitedmessengers.util.addNewItem
import com.progcorp.unitedmessengers.util.removeItem
import com.progcorp.unitedmessengers.util.updateItemAt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class VKClient (private val _sharedPreference: SharedPreferences) {
    private var _dataSource: VKDataSource = VKDataSource(this)
    var repository: VKRepository = VKRepository(_dataSource)

    private val _authStatus = MutableLiveData<VKAuthStatus>()
    val authStatus: LiveData<VKAuthStatus> = _authStatus

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    var lpServer: VKLongPollServer? = null
    var lpRetrofit: Retrofit? = null
    var lpHistory: String? = null

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _unreadCount = MutableLiveData<Int?>()
    val unreadCount: LiveData<Int?> = _unreadCount

    val conversationsList = MediatorLiveData<MutableList<Conversation>>()
    var conversationsViewModel: VKConversationsViewModel? = null

    private val _updateResult = MutableSharedFlow<VKUpdate?>(
        replay = 0,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    val updateResult: SharedFlow<VKUpdate?> = _updateResult

    init {
        _authStatus.value = if (token != null) VKAuthStatus.SUCCESS else VKAuthStatus.AUTH
        Log.d("VKClient", "authStatus: ${authStatus.value}")
        setAuth(_authStatus.value!!)
        startHandlers()
    }

    private fun startHandlers() {
        MainScope().launch(Dispatchers.Main) {
            updateResult.collect { update ->
                Log.d("VKClient", "onResult: ${update?.javaClass?.simpleName}")
                when (update) {
                    is VKUpdateNewMessages -> {
                        for (message in update.messages) {
                            val item = conversationsList.value?.find {
                                it.id == message.conversationId
                            }
                            if (item != null) {
                                conversationsList.value?.indexOf(item)?.let { prev ->
                                    item.lastMessage = message
                                    conversationsList.value?.sortByDescending {
                                        it.lastMessage?.timeStamp
                                    }
                                    conversationsList.value?.indexOf(item)?.let { new ->
                                        conversationsViewModel?.updateLastMessage(prev, new)
                                    }
                                }
                            }
                            else {
                                val conversation =
                                    repository.getConversationById(message.conversationId).first()
                                conversation?.let {
                                    it.lastMessage = message
                                    conversationsList.addFrontItem(it)
                                    conversationsViewModel?.addNewChat()
                                }
                            }
                        }
                    }
                    is VKUpdateMessagesContent -> {
                        for (message in update.messages) {
                            conversationsList.value?.find {
                                it.id == message.conversationId
                            }?.let { conversation ->
                                if (conversation.lastMessage?.id == message.id) {
                                    conversation.lastMessage = message
                                    conversationsList.value?.indexOf(conversation)?.let { index ->
                                        conversationsViewModel?.updateLastMessageContent(index)
                                    }
                                }
                            }
                        }
                    }
                    is VKUpdateUserStatus -> {
                        val item = conversationsList.value?.find {
                            it.companion is User && it.companion.id == update.userId
                        }
                        item?.vkParseOnlineStatus(update)
                        conversationsList.value?.indexOf(item)?.let {
                            conversationsViewModel?.updateOnline(it)
                        }
                    }
                    is VKUpdateDeleteMessage -> {}
                    is VKUpdateUnreadCount -> {
                        _unreadCount.postValue(update.count)
                    }
                }
            }
        }
    }

    private fun initLongPoll() {
        MainScope().launch {
            lpServer = repository.getLongPollServer().first()
            lpRetrofit = Retrofit.Builder()
                .baseUrl("https://im.vk.com/")
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
            startLongPolling()
        }
    }

    private suspend fun startLongPolling() {
        while (true) {
            lpHistory = _dataSource.getLongPollHistory().data
            lpHistory?.let { lpServer?.parseResponse(JSONObject(it)) }
            Log.d("retrofit", lpHistory.toString())
        }
    }

    suspend fun changeUpdateState(update: VKUpdate) {
        _updateResult.emit(update)
    }

    private fun getUser() {
        MainScope().launch {
            val data = repository.getUser().first()
            _user.postValue(data[0])
        }
    }

    private fun setAuth(auth: VKAuthStatus) {
        if (auth == VKAuthStatus.SUCCESS) {
            getUser()
            startGetter()
            initLongPoll()
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
        MainScope().launch {
            val data = repository.getUnreadCount().first()
            _unreadCount.value = data
        }
        conversationsList.value?.sortByDescending { it.lastMessage?.timeStamp }
    }

    private fun startGetter() {
        loadConversations(0, true)
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