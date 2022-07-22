package com.progcorp.unitedmessengers.ui.login.telegram

import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.enums.TelegramAuthStatus
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

class TelegramAuthViewModelFactory :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TelegramAuthViewModel() as T
    }
}

class TelegramAuthViewModel : ViewModel() {

    enum class LayoutState {
        LOADING, INSERT_NUMBER, INSERT_CODE, INSERT_PASSWORD, AUTHENTICATED
    }

    private val _client = App.application.tgClient

    private val _restartEvent = MutableLiveData<Event<Unit>>()
    val restartEvent: LiveData<Event<Unit>> = _restartEvent

    private val _authEvent = MutableLiveData<Event<Unit>>()
    val authEvent: LiveData<Event<Unit>> = _authEvent

    private val _showPhoneEvent = MutableLiveData<Event<Unit>>()
    val showPhoneEvent: LiveData<Event<Unit>> = _showPhoneEvent

    private val _showPasswordEvent = MutableLiveData<Event<Unit>>()
    val showPasswordEvent: LiveData<Event<Unit>> = _showPasswordEvent

    private val _showCodeEvent = MutableLiveData<Event<Unit>>()
    val showCodeEvent: LiveData<Event<Unit>> = _showCodeEvent

    private val _hideAllEvent = MutableLiveData<Event<Unit>>()
    val hideAllEvent: LiveData<Event<Unit>> = _hideAllEvent

    private val layoutState = MutableLiveData<LayoutState>()
    val phoneNumberText = MutableLiveData<String?>()
    val codeText = MutableLiveData<String?>()
    val passwordText = MutableLiveData<String?>()

    init {
        observeAuthStatus()
    }

    private fun observeAuthStatus() {
        _client.authState.onEach {
            when (it) {
                TelegramAuthStatus.UNAUTHENTICATED, TelegramAuthStatus.UNKNOWN -> {
                    layoutState.value = LayoutState.LOADING
                    _hideAllEvent.value = Event(Unit)
                }
                TelegramAuthStatus.WAIT_FOR_NUMBER -> {
                    layoutState.value = LayoutState.INSERT_NUMBER
                    _showPhoneEvent.value = Event(Unit)
                }
                TelegramAuthStatus.WAIT_FOR_CODE -> {
                    layoutState.value = LayoutState.INSERT_CODE
                    _showCodeEvent.value = Event(Unit)
                }
                TelegramAuthStatus.WAIT_FOR_PASSWORD -> {
                    layoutState.value = LayoutState.INSERT_PASSWORD
                    _showPasswordEvent.value = Event(Unit)
                }
                TelegramAuthStatus.AUTHENTICATED -> {
                    layoutState.value = LayoutState.AUTHENTICATED
                    _authEvent.value = Event(Unit)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun insertPhoneNumber() {
        if (!phoneNumberText.value.isNullOrBlank()) {
            layoutState.value = LayoutState.LOADING
            _hideAllEvent.value = Event(Unit)
            _client.insertPhoneNumber(phoneNumberText.value!!)
        }
    }

    fun insertCode() {
        if (!codeText.value.isNullOrBlank()) {
            layoutState.value = LayoutState.LOADING
            _hideAllEvent.value = Event(Unit)
            _client.insertCode(codeText.value!!)
        }
    }

    fun insertPassword() {
        if (!passwordText.value.isNullOrBlank()) {
            layoutState.value = LayoutState.LOADING
            _hideAllEvent.value = Event(Unit)
            _client.insertPassword(passwordText.value!!)
        }
    }

    fun logout() {
        layoutState.value = LayoutState.LOADING
        _client.logout()
        _restartEvent.value = Event(Unit)
    }
}