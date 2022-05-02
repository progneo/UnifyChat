package com.progcorp.unitedmessengers.ui.login.telegram

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.util.Authentication
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

enum class LayoutState {
    LOADING, INSERT_NUMBER, INSERT_CODE, INSERT_PASSWORD, AUTHENTICATED
}

class TelegramAuthViewModel : ViewModel() {

    val layoutState = MediatorLiveData<LayoutState>()
    val phoneNumberText = MutableLiveData<String?>()
    val codeText = MutableLiveData<String?>()
    val passwordText = MutableLiveData<String?>()

    init {
        App.application.tgClient.authState.onEach {
            when (it) {
                Authentication.UNAUTHENTICATED, Authentication.UNKNOWN -> {
                    layoutState.value = LayoutState.LOADING
                }
                Authentication.WAIT_FOR_NUMBER -> {
                    layoutState.value = LayoutState.INSERT_NUMBER
                }
                Authentication.WAIT_FOR_CODE -> {
                    layoutState.value =LayoutState.INSERT_CODE
                }
                Authentication.WAIT_FOR_PASSWORD -> {
                    layoutState.value = LayoutState.INSERT_PASSWORD
                }
                Authentication.AUTHENTICATED -> {
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
        }
    }

    fun insertPassword() {
        if (!passwordText.value.isNullOrBlank()) {
            layoutState.value = LayoutState.LOADING
            App.application.tgClient.insertPassword(passwordText.value!!)
        }
    }
}