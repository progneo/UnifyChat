package com.progcorp.unitedmessengers.ui.login.telegram

import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.ui.DefaultViewModel
import com.progcorp.unitedmessengers.enums.TelegramAuthStatus
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

class TelegramAuthViewModelFactory :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TelegramAuthViewModel() as T
    }
}

class TelegramAuthViewModel : DefaultViewModel() {

    enum class LayoutState {
        LOADING, INSERT_NUMBER, INSERT_CODE, INSERT_PASSWORD, AUTHENTICATED
    }

    private val _restartEvent = MutableLiveData<Event<Unit>>()

    val layoutState = MediatorLiveData<LayoutState>()
    val phoneNumberText = MutableLiveData<String?>()
    val codeText = MutableLiveData<String?>()
    val passwordText = MutableLiveData<String?>()

    val restartEvent: LiveData<Event<Unit>> = _restartEvent

    init {
        App.application.tgClient.authState.onEach {
            when (it) {
                TelegramAuthStatus.UNAUTHENTICATED, TelegramAuthStatus.UNKNOWN -> {
                    layoutState.value = LayoutState.LOADING
                }
                TelegramAuthStatus.WAIT_FOR_NUMBER -> {
                    layoutState.value = LayoutState.INSERT_NUMBER
                }
                TelegramAuthStatus.WAIT_FOR_CODE -> {
                    layoutState.value = LayoutState.INSERT_CODE
                }
                TelegramAuthStatus.WAIT_FOR_PASSWORD -> {
                    layoutState.value = LayoutState.INSERT_PASSWORD
                }
                TelegramAuthStatus.AUTHENTICATED -> {
                    layoutState.value = LayoutState.AUTHENTICATED
                }
            }
        }.launchIn(viewModelScope)
    }

    fun insertPhoneNumber() {
        if (!phoneNumberText.value.isNullOrBlank()) {
            layoutState.value = LayoutState.LOADING
            App.application.tgClient.insertPhoneNumber(phoneNumberText.value!!)
        }
    }

    fun insertCode() {
        if (!codeText.value.isNullOrBlank()) {
            layoutState.value = LayoutState.LOADING
            App.application.tgClient.insertCode(codeText.value!!)
            _restartEvent.value = Event(Unit)
        }
    }

    fun insertPassword() {
        if (!passwordText.value.isNullOrBlank()) {
            layoutState.value = LayoutState.LOADING
            App.application.tgClient.insertPassword(passwordText.value!!)
        }
    }

    fun logout() {
        layoutState.value = LayoutState.LOADING
        App.application.tgClient.logout()
    }
}