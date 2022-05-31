@file:OptIn(ExperimentalCoroutinesApi::class)

package com.progcorp.unitedmessengers.data.clients

import android.util.Log
import com.progcorp.unitedmessengers.enums.TelegramAuthStatus
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel
import com.progcorp.unitedmessengers.ui.conversations.telegram.TelegramConversationsViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class TelegramClient (private val tdLibParameters: TdApi.TdlibParameters) : Client.ResultHandler {
    lateinit var client: Client

    private val _authState = MutableStateFlow(TelegramAuthStatus.UNKNOWN)
    val authState: StateFlow<TelegramAuthStatus> get() = _authState

    var conversationsViewModel: TelegramConversationsViewModel? = null
    var conversationViewModel: ConversationViewModel? = null

    init {
        setupClient()
    }

    private fun setupClient() {
        client = Client.create(this, null, null)!!
        client.send(TdApi.SetLogVerbosityLevel(1), this)
        client.send(TdApi.SetTdlibParameters(tdLibParameters), this)
        client.send(TdApi.GetAuthorizationState(), this)
    }

    fun close() {
        client.close()
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
                conversationsViewModel?.updateOnline(data as TdApi.UpdateUserStatus)
                conversationViewModel?.updateOnline(data as TdApi.UpdateUserStatus)
            }

            TdApi.UpdateChatLastMessage.CONSTRUCTOR -> {
                conversationsViewModel?.updateLastMessage(data as TdApi.UpdateChatLastMessage)
            }

            TdApi.UpdateChatReadInbox.CONSTRUCTOR -> {
                conversationsViewModel?.updateReadInbox(data as TdApi.UpdateChatReadInbox)
            }

            TdApi.UpdateNewChat.CONSTRUCTOR -> {
                conversationsViewModel?.addNewChat(data as TdApi.UpdateNewChat)
            }

            TdApi.UpdateNewMessage.CONSTRUCTOR -> {
                conversationsViewModel?.updateNewMessage(data as TdApi.UpdateNewMessage)
                conversationViewModel?.newMessage(data as TdApi.UpdateNewMessage)
            }
            //TODO:
            //TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR -> {

            //}

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
        client.send(TdApi.LogOut(), this)
        setupClient()
    }

    private fun doAsync(job: () -> Unit) {
        requestScope.launch { job() }
    }

    fun startAuthentication() {
        Log.d(TAG, "startAuthentication called")
        when (_authState.value) {
            TelegramAuthStatus.AUTHENTICATED -> {
                Log.w(TAG, "Start authentication called but client already authenticated. State: ${_authState.value}.")
                return
            }
            TelegramAuthStatus.WAIT_FOR_CODE -> {
                Log.w(TAG, "Restart authentication. State: ${_authState.value}.")
                logout()
            }
            TelegramAuthStatus.WAIT_FOR_PASSWORD -> {
                Log.w(TAG, "Restart authentication. State: ${_authState.value}.")
                logout()
            }
            else -> {
                Log.w(TAG, "Start authentication. State: ${_authState.value}.")
            }
        }

        doAsync {
            client.send(TdApi.SetTdlibParameters(tdLibParameters)) {
                Log.d(TAG, "SetTdlibParameters result: $it")
                when (it.constructor) {
                    TdApi.Ok.CONSTRUCTOR -> {
                        //result.postValue(true)
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        //result.postValue(false)
                    }
                }
            }
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
        client.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, settings)) {
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
            client.send(TdApi.CheckAuthenticationCode(code)) {
                when (it.constructor) {
                    TdApi.Ok.CONSTRUCTOR -> {

                    }
                    TdApi.Error.CONSTRUCTOR -> {

                    }
                }
            }
        }
    }

    fun insertPassword(password: String) {
        Log.d("TelegramClient", "inserting password")
        doAsync {
            client.send(TdApi.CheckAuthenticationPassword(password)) {
                when (it.constructor) {
                    TdApi.Ok.CONSTRUCTOR -> {

                    }
                    TdApi.Error.CONSTRUCTOR -> {

                    }
                }
            }
        }
    }

    private fun onAuthorizationStateUpdated(authorizationState: TdApi.AuthorizationState) {
        when (authorizationState.constructor) {
            TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                Log.d(
                    TAG,
                    "onResult: AuthorizationStateWaitTdlibParameters -> state = UNAUTHENTICATED"
                )
                setAuth(TelegramAuthStatus.UNAUTHENTICATED)
            }
            TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateWaitEncryptionKey")
                client.send(TdApi.CheckDatabaseEncryptionKey()) {
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

    fun downloadableFile(file: TdApi.File): Flow<String?> =
        file.takeIf {
            it.local?.isDownloadingCompleted == false
        }?.id?.let { fileId ->
            downloadFile(fileId).map { file.local?.path }
        } ?: flowOf(file.local?.path)

    private fun downloadFile(fileId: Int): Flow<Unit> = callbackFlow {
        client.send(TdApi.DownloadFile(fileId, 1, 0, 0, true)) {
            when (it.constructor) {
                TdApi.Ok.CONSTRUCTOR -> {
                    trySend(Unit).isSuccess
                }
                else -> {
                    cancel("", Exception(""))

                }
            }
        }
        awaitClose()
    }

    fun sendAsFlow(query: TdApi.Function): Flow<TdApi.Object> = callbackFlow {
        client.send(query) {
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

    inline fun <reified T : TdApi.Object> send(query: TdApi.Function): Flow<T> =
        sendAsFlow(query).map { it as T }

    companion object {
        const val TAG = "TelegramClient"
    }
}