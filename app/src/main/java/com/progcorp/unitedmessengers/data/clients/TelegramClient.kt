package com.progcorp.unitedmessengers.data.clients

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.progcorp.unitedmessengers.data.db.TelegramDataSource
import com.progcorp.unitedmessengers.data.model.*
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.enums.TelegramAuthStatus
import com.progcorp.unitedmessengers.interfaces.IClient
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel
import com.progcorp.unitedmessengers.ui.conversations.telegram.TelegramConversationsViewModel
import com.progcorp.unitedmessengers.util.addFrontItem
import com.progcorp.unitedmessengers.util.addNewItem
import com.progcorp.unitedmessengers.util.updateItemAt
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class TelegramClient (
        private val _tdLibParameters: TdApi.TdlibParameters
    ) : Client.ResultHandler, IClient {
    var client: Client? = null

    val repository: TelegramDataSource = TelegramDataSource(this)

    private val _authState = MutableStateFlow(TelegramAuthStatus.UNKNOWN)
    val authState: StateFlow<TelegramAuthStatus> get() = _authState

    private val _currentConversation = MutableLiveData<Conversation?>()
    override val currentConversation: LiveData<Conversation?> = _currentConversation

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _unreadCount = MutableLiveData<Int?>()
    val unreadCount: LiveData<Int?> = _unreadCount

    private val _isLoaded = MutableLiveData(false)
    val isLoaded: LiveData<Boolean> = _isLoaded

    override val conversationsList = MediatorLiveData<MutableList<Conversation>>()
    override val messagesList = MediatorLiveData<MutableList<Message>>()

    private val _oldMessage = MutableLiveData<Message>()
    private val _newMessage = MutableLiveData<Message>()

    var conversationsViewModel: TelegramConversationsViewModel? = null
    override var conversationViewModel: ConversationViewModel? = null

    //Init
    init {
        setupClient()
        addSources()
    }

    private fun setupClient() {
        client = Client.create(this, null, null)!!
        client?.let {
            it.send(TdApi.SetLogVerbosityLevel(1), this)
            it.send(TdApi.SetTdlibParameters(_tdLibParameters), this)
            it.send(TdApi.GetAuthorizationState(), this)
        }
    }

    private fun addSources() {
        messagesList.addSource(_oldMessage) { message ->
            currentConversation.value?.let {
                messagesList.value?.find { it.id == message.id }?.let {
                    messagesList.updateItemAt(it, messagesList.value!!.indexOf(it))
                } ?: run {
                    messagesList.addNewItem(message)
                }
                messagesList.value?.sortByDescending { it.id }
            }
        }

        messagesList.addSource(_newMessage) { message ->
            currentConversation.value?.let {
                messagesList.value?.find { it.id == message.id }?.let {
                    messagesList.updateItemAt(it, messagesList.value!!.indexOf(it))
                } ?: run {
                    messagesList.addFrontItem(message)
                }
                messagesList.value?.sortByDescending { it.id }
            }
        }
    }

    //Login states
    private fun setAuth(auth: TelegramAuthStatus) {
        _authState.value = auth
        if (_authState.value == TelegramAuthStatus.AUTHENTICATED) {
            MainScope().launch {
                conversationsList.value?.clear()
                getUser()
                fetchChats()
            }
        }
        else {
            _isLoaded.value = true
        }
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

    fun logout() {
        client?.send(TdApi.LogOut(), this)
        setupClient()
    }

    //Handlers
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

    override fun onResult(data: TdApi.Object) {
        Log.d(TAG, "onResult: ${data::class.java.simpleName}")
        when (data.constructor) {
            TdApi.UpdateAuthorizationState.CONSTRUCTOR -> {
                onAuthorizationStateUpdated((data as TdApi.UpdateAuthorizationState).authorizationState)
            }
            //TODO: Notify viewmodel
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
                _currentConversation.value?.let {
                    if (data.userId == it.id) {
                        MainScope().launch {
                            it.tgParseOnlineStatus(data)
                        }
                    }
                    _currentConversation.postValue(it.copy())
                }
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
            //TODO: Check how it works on outgoing messages
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
                MainScope().launch() {
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
                _currentConversation.value?.let {
                    if (data.message.chatId == it.id) {
                        MainScope().launch() {
                            val message = Message.tgParse(data.message)
                            _newMessage.value = message
                        }
                    }
                }
            }

            TdApi.UpdateUnreadChatCount.CONSTRUCTOR -> {
                val update = (data as TdApi.UpdateUnreadChatCount)
                MainScope().launch {
                    if (update.chatList.constructor == TdApi.ChatListMain.CONSTRUCTOR) {
                        _unreadCount.value = update.unreadCount
                    }
                }
            }

            TdApi.UpdateMessageSendSucceeded.CONSTRUCTOR -> {
                val update = (data as TdApi.UpdateMessageSendSucceeded)
                MainScope().launch {
                    messagesList.value?.let { messagesList ->
                        messagesList.find { it.id == update.oldMessageId }?.let {
                            it.id = update.message.id
                            it.canBeEdited = update.message.canBeEdited
                            it.canBeDeletedForAllUsers = update.message.canBeDeletedForAllUsers
                            it.canBeDeletedOnlyForSelf = update.message.canBeDeletedOnlyForSelf
                        } ?: run {
                            _newMessage.value = Message.tgParse(update.message)
                        }
                    }
                }
            }

            TdApi.UpdateMessageContent.CONSTRUCTOR -> {
                val update = (data as TdApi.UpdateMessageContent)
                MainScope().launch {
                    currentConversation.value?.let { conversation ->
                        if (conversation.id == update.chatId) {
                            messagesList.value?.let { list ->
                                list.find { it.id == update.messageId }?.let { message ->
                                    message.updateMessageContent(update.newContent)
                                    conversationViewModel?.messageEdited(list.indexOf(message))
                                }
                            }
                        }
                    }
                }
            }

            TdApi.UpdateDeleteMessages.CONSTRUCTOR -> {

            }

            //TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR -> {

            //}

            //TdApi.UpdateUser.CONSTRUCTOR -> {

            //}

            //TdApi.UpdateOption.CONSTRUCTOR -> {

            //}

            else -> Log.d(TAG, "Unhandled onResult call with data: $data.")
        }
    }

    //Async functions
    private val requestScope = CoroutineScope(Dispatchers.IO)

    private fun doAsync(job: () -> Unit) {
        requestScope.launch { job() }
    }

    private fun doAsyncSuspend(job: suspend () -> Unit) {
        requestScope.launch { job() }
    }

    suspend fun download(id: Int): String? {
        var file = id.let {
            repository.getFile(it).first()
        }
        if (file.local?.isDownloadingCompleted == false) {
            file = repository.downloadFile(file.id).first()
        }
        return file.local?.path
    }

    private suspend fun getUser() {
        val data = repository.getMe().first()
        val user = User.tgParse(data)
        _user.postValue(user)
    }

    private suspend fun fetchChats() {
        val response = repository.loadConversations(100).first()
        if (response.constructor == TdApi.Ok.CONSTRUCTOR) {
            Log.d(TAG, "Conversations successfully updated.")
        }
        else {
            Log.d(TAG, "Can't update conversations list.")
        }
        val data = repository.getConversations(100).first()
        for (conversation in data) {
            Conversation.tgParse(conversation)?.let {
                conversationsList.addNewItem(it)
            }
        }
        conversationsList.value?.sortByDescending {
            it.lastMessage?.timeStamp
        }
        _isLoaded.value = true
    }

    //Conversation functions
    override fun setConversation(conversation: Conversation?) {
        _currentConversation.value = conversation
    }

    override suspend fun loadLatestMessages() {
        _currentConversation.value?.let {
            val data = repository.getMessages(it.id, 0,20).first()
            for (item in data) {
                _newMessage.value = Message.tgParse(item)
            }
        }
    }

    override suspend fun loadMessagesFromId(messageId: Long) {
        _currentConversation.value?.let {
            val data = repository.getMessages(it.id, messageId, 20).first()
            for (item in data) {
                _oldMessage.value = Message.tgParse(item)
            }
        }
    }

    override suspend fun sendMessage(message: Message) {
        MainScope().launch(Dispatchers.IO) {
            _currentConversation.value?.let {
                repository.sendMessage(it.id, message).first()
            }
        }
    }

    override suspend fun editMessage(message: Message) {
        _currentConversation.value?.let {
            when (message.content) {
                is MessageText -> {
                    repository.editMessageText(it.id, message).first()
                }
                is MessagePhoto -> {
                    repository.editMessageCaption(it.id, message).first()
                }
                is MessageAnimation -> {
                    repository.editMessageCaption(it.id, message).first()
                }
                is MessageVideo -> {
                    repository.editMessageCaption(it.id, message).first()
                }
                is MessageVoiceNote -> {
                    repository.editMessageCaption(it.id, message).first()
                }
                is MessageDocument -> {
                    repository.editMessageCaption(it.id, message).first()
                }
                else -> {}
            }
        }
    }

    override suspend fun deleteMessages(messages: List<Message>, forAll: Boolean) {
        _currentConversation.value?.let {
            repository.deleteMessages(it.id, messages, forAll).first()
        }
    }

    fun sendAsFlow(query: TdApi.Function): Flow<TdApi.Object> = callbackFlow {
        client?.send(query) {
            when (it.constructor) {
                TdApi.Error.CONSTRUCTOR -> {
                    Log.e(TAG, "Unknown error")
                    error("Unknown error")
                }
                else -> {
                    trySend(it).isSuccess
                }
            }
        }
        awaitClose { }
    }

    inline fun <reified T : TdApi.Object> send(query: TdApi.Function): Flow<T> =
        sendAsFlow(query).map { it as T }

    companion object {
        const val TAG = "TelegramClient"
    }
}