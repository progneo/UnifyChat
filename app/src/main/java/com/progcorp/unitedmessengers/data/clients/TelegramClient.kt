package com.progcorp.unitedmessengers.data.clients

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import com.progcorp.unitedmessengers.data.db.TelegramDataSource
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.enums.TelegramAuthStatus
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel
import com.progcorp.unitedmessengers.ui.conversations.telegram.TelegramConversationsViewModel
import com.progcorp.unitedmessengers.util.addFrontItem
import com.progcorp.unitedmessengers.util.addNewItem
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class TelegramClient (private val _tdLibParameters: TdApi.TdlibParameters) : Client.ResultHandler {
    private val _authState = MutableStateFlow(TelegramAuthStatus.UNKNOWN)
    val authState: StateFlow<TelegramAuthStatus> get() = _authState

    var client: Client? = null

    val repository: TelegramDataSource = TelegramDataSource(this)

    val conversationsList = MediatorLiveData<MutableList<Conversation>>()

    var conversationsViewModel: TelegramConversationsViewModel? = null
    var conversationViewModel: ConversationViewModel? = null

    init {
        setupClient()
    }

    private fun setupClient() {
        client = Client.create(this, null, null)!!
        client?.let {
            it.send(TdApi.SetLogVerbosityLevel(1), this)
            it.send(TdApi.SetTdlibParameters(_tdLibParameters), this)
            it.send(TdApi.GetAuthorizationState(), this)
        }
    }

    private val requestScope = CoroutineScope(Dispatchers.IO)

    private fun setAuth(auth: TelegramAuthStatus) {
        _authState.value = auth
    }

    override fun onResult(data: TdApi.Object) {
        Log.d(TAG, "onResult: ${data::class.java.simpleName}")
        when (data.constructor) {
            TdApi.UpdateAuthorizationState.CONSTRUCTOR -> {
                onAuthorizationStateUpdated((data as TdApi.UpdateAuthorizationState).authorizationState)
            }

            TdApi.UpdateUserStatus.CONSTRUCTOR -> {
                val update = (data as TdApi.UpdateUserStatus)
                MainScope().launch {
                    val item = conversationsList.value?.find {
                        it.companion is User && it.companion.id == update.userId
                    }
                    item?.tgParseOnlineStatus(update)
                    conversationsList.value?.indexOf(item)?.let {
                        conversationsViewModel?.updateOnline(it)
                    }

                }
                conversationViewModel?.updateOnline(update)
            }

            TdApi.UpdateChatLastMessage.CONSTRUCTOR -> {
                val update = (data as TdApi.UpdateChatLastMessage)
                MainScope().launch {
                    val item = conversationsList.value?.find {
                        it.id == update.chatId
                    }
                    if (item != null) {
                        conversationsList.value?.indexOf(item)?.let { prev ->
                            item.tgParseLastMessage(update)
                            conversationsList.value?.sortByDescending {
                                it.lastMessage?.timeStamp
                            }
                            conversationsList.value?.indexOf(item)?.let { new ->
                                conversationsViewModel?.updateLastMessage(prev, new)
                            }
                        }
                    }
                }
            }

            TdApi.UpdateChatReadInbox.CONSTRUCTOR -> {
                val update = (data as TdApi.UpdateChatReadInbox)
                MainScope().launch {
                    val item = conversationsList.value?.find {
                        it.id == update.chatId
                    }
                    item?.unreadCount = update.unreadCount
                    conversationsList.value?.indexOf(item)?.let {
                        conversationsViewModel?.updateReadInbox(it)
                    }
                }
            }

            TdApi.UpdateNewChat.CONSTRUCTOR -> {
                val update = (data as TdApi.UpdateNewChat)
                MainScope().launch {
                    val conversation = conversationsList.value?.find {
                        it.id == update.chat.id
                    }
                    if (conversation == null) {
                        Conversation.tgParse(update.chat)?.let { chat ->
                            conversationsList.addFrontItem(chat)
                            conversationsList.value?.let {
                                it.sortByDescending { conversation ->
                                    conversation.lastMessage?.timeStamp
                                }
                                conversationsViewModel?.addNewChat(it.indexOf(chat))
                            }
                        }
                    }
                }
            }

            TdApi.UpdateNewMessage.CONSTRUCTOR -> {
                val update = (data as TdApi.UpdateNewMessage)
                MainScope().launch {
                    val item = conversationsList.value?.find {
                        it.id == update.message.chatId
                    }
                    if (item != null) {
                        conversationsList.value?.indexOf(item)?.let { prev ->
                            item.tgParseNewMessage(update)
                            conversationsList.value?.sortByDescending {
                                it.lastMessage?.timeStamp
                            }
                            conversationsList.value?.indexOf(item)?.let { new ->
                                conversationsViewModel?.updateNewMessage(prev, new)
                            }
                        }
                    }
                }
                conversationViewModel?.newMessage(data)
            }
            //TODO:
            TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR -> {

            }

            //TdApi.UpdateUser.CONSTRUCTOR -> {

            //}

            //TdApi.UpdateOption.CONSTRUCTOR -> {

            //}
            //TdApi.UpdateUnreadMessageCount.CONSTRUCTOR -> {
            //
            //}

            else -> Log.d(TAG, "Unhandled onResult call with data: $data.")
        }
    }

    fun logout() {
        client?.send(TdApi.LogOut(), this)
        setupClient()
    }

    private fun doAsync(job: () -> Unit) {
        requestScope.launch { job() }
    }

    private fun doAsyncSuspend(job: suspend () -> Unit) {
        requestScope.launch { job() }
    }

    fun insertPhoneNumber(phoneNumber: String) {
        Log.d("TelegramClient", "phoneNumber: $phoneNumber")
        val settings = TdApi.PhoneNumberAuthenticationSettings(
            false,
            false,
            false,
            false,
            arrayOf(String())
        )
        client?.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, settings)) {
            Log.d("TelegramClient", "phoneNumber. result: $it")
            when (it.constructor) {
                TdApi.Ok.CONSTRUCTOR -> {

                }
                TdApi.Error.CONSTRUCTOR -> {

                }
            }
        }
    }

    fun insertCode(code: String) {
        Log.d("TelegramClient", "code: $code")
        doAsync {
            client?.send(TdApi.CheckAuthenticationCode(code)) {
                when (it.constructor) {
                    TdApi.Ok.CONSTRUCTOR -> {}
                    TdApi.Error.CONSTRUCTOR -> {}
                }
            }
        }
    }

    fun insertPassword(password: String) {
        Log.d("TelegramClient", "inserting password")
        doAsync {
            client?.send(TdApi.CheckAuthenticationPassword(password)) {
                when (it.constructor) {
                    TdApi.Ok.CONSTRUCTOR -> {}
                    TdApi.Error.CONSTRUCTOR -> {}
                }
            }
        }
    }

    private fun onAuthorizationStateUpdated(authorizationState: TdApi.AuthorizationState) {
        when (authorizationState.constructor) {
            TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateWaitTdlibParameters -> state = UNAUTHENTICATED"
                )
                setAuth(TelegramAuthStatus.UNAUTHENTICATED)
            }
            TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateWaitEncryptionKey")
                client?.send(TdApi.CheckDatabaseEncryptionKey()) {
                    when (it.constructor) {
                        TdApi.Ok.CONSTRUCTOR -> {
                            Log.d(TAG, "CheckDatabaseEncryptionKey: OK")
                        }
                        TdApi.Error.CONSTRUCTOR -> {
                            Log.d(TAG, "CheckDatabaseEncryptionKey: Error")
                        }
                    }
                }
            }
            TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateWaitPhoneNumber -> state = WAIT_FOR_NUMBER")
                setAuth(TelegramAuthStatus.WAIT_FOR_NUMBER)
            }
            TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateWaitCode -> state = WAIT_FOR_CODE")
                setAuth(TelegramAuthStatus.WAIT_FOR_CODE)
            }
            TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateWaitPassword")
                setAuth(TelegramAuthStatus.WAIT_FOR_PASSWORD)
            }
            TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateReady -> state = AUTHENTICATED")
                setAuth(TelegramAuthStatus.AUTHENTICATED)
            }
            TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateLoggingOut")
                setAuth(TelegramAuthStatus.UNAUTHENTICATED)
            }
            TdApi.AuthorizationStateClosing.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateClosing")
            }
            TdApi.AuthorizationStateClosed.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateClosed")
            }
            else -> Log.d(TAG, "Unhandled authorizationState with data: $authorizationState.")
        }
    }

    suspend fun download(id: Int): String? {
        var file = id.let {
            repository.getFile(it).first()
        }
        if (file.local?.isDownloadingCompleted == false) {
            file = downloadFile(file.id).first()
        }
        return file.local?.path
    }

    private fun downloadFile(fileId: Int): Flow<TdApi.File> = callbackFlow {
        client?.send(TdApi.DownloadFile(fileId, 1, 0, 0, true)) {
            when (it.constructor) {
                TdApi.File.CONSTRUCTOR -> {
                    trySend((it as TdApi.File)).isSuccess
                }
                else -> {
                    Log.e(
                        "${javaClass.simpleName}.downloadFile",
                        "Unknown error"
                    )
                    cancel("", Exception(""))
                }
            }
        }
        awaitClose()
    }

    fun sendAsFlow(query: TdApi.Function): Flow<TdApi.Object> = callbackFlow {
        client?.send(query) {
            when (it.constructor) {
                TdApi.Error.CONSTRUCTOR -> {
                    error("")
                }
                else -> {
                    trySend(it).isSuccess
                }
            }
        }
        awaitClose { }
    }

    fun fetchChats() {
        MainScope().launch {
            val data = repository.getConversations(100).first()
            for (conversation in data) {
                Conversation.tgParse(conversation)?.let {
                    conversationsList.addNewItem(it)
                }
            }
        }
    }

    inline fun <reified T : TdApi.Object> send(query: TdApi.Function): Flow<T> =
        sendAsFlow(query).map { it as T }

    companion object {
        const val TAG = "TelegramClient"
    }
}