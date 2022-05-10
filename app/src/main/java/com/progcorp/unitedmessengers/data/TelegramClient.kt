package com.progcorp.unitedmessengers.data

import android.util.Log
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.model.ConversationsList
import com.progcorp.unitedmessengers.ui.conversations.telegram.TelegramConversationsViewModel
import com.progcorp.unitedmessengers.util.Authentication
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

@OptIn(ExperimentalCoroutinesApi::class)
class TelegramClient(private val tdLibParameters: TdApi.TdlibParameters) : Client.ResultHandler {
    lateinit var client: Client

    private val _authState = MutableStateFlow(Authentication.UNKNOWN)
    val authState: StateFlow<Authentication> get() = _authState

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

    private fun setAuth(auth: Authentication) {
        _authState.value = auth
        if (auth == Authentication.AUTHENTICATED) {
            App.application.tgConversationsList = ConversationsList()
        }
    }

    override fun onResult(data: TdApi.Object) {
        Log.d(TAG, "onResult: ${data::class.java.simpleName}")
        when (data.constructor) {
            TdApi.UpdateAuthorizationState.CONSTRUCTOR -> {
                onAuthorizationStateUpdated((data as TdApi.UpdateAuthorizationState).authorizationState)
            }

            TdApi.UpdateUserStatus.CONSTRUCTOR -> {
                App.application.tgConversationsList.updateOnline(data as TdApi.UpdateUserStatus)

            }

            TdApi.UpdateChatLastMessage.CONSTRUCTOR -> {
                App.application.tgConversationsList.updateLastMessage(data as TdApi.UpdateChatLastMessage)
            }

            TdApi.UpdateChatReadInbox.CONSTRUCTOR -> {
                App.application.tgConversationsList.updateReadInbox(data as TdApi.UpdateChatReadInbox)
            }

            TdApi.UpdateNewChat.CONSTRUCTOR -> {
                App.application.tgConversationsList.addNewChat(data as TdApi.UpdateNewChat)
            }

            TdApi.UpdateNewMessage.CONSTRUCTOR -> {
                App.application.tgConversationsList.updateNewMessage(data as TdApi.UpdateNewMessage)
            }

            TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR -> {

            }

            TdApi.UpdateUser.CONSTRUCTOR -> {

            }

            TdApi.UpdateOption.CONSTRUCTOR -> {

            }

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
            Authentication.AUTHENTICATED -> {
                Log.w(TAG, "Start authentication called but client already authenticated. State: ${_authState.value}.")
                return
            }
            Authentication.WAIT_FOR_CODE -> {
                Log.w(TAG, "Restart authentication. State: ${_authState.value}.")
                logout()
            }
            Authentication.WAIT_FOR_PASSWORD -> {
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
                setAuth(Authentication.UNAUTHENTICATED)
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
                setAuth(Authentication.WAIT_FOR_NUMBER)
            }
            TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateWaitCode -> state = WAIT_FOR_CODE")
                setAuth(Authentication.WAIT_FOR_CODE)
            }
            TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateWaitPassword")
                setAuth(Authentication.WAIT_FOR_PASSWORD)
            }
            TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateReady -> state = AUTHENTICATED")
                setAuth(Authentication.AUTHENTICATED)
            }
            TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR -> {
                Log.d(TAG, "onResult: AuthorizationStateLoggingOut")
                setAuth(Authentication.UNAUTHENTICATED)
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

    fun downloadFile(fileId: Int): Flow<Unit> = callbackFlow {
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