package com.progcorp.unitedmessengers.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.progcorp.unitedmessengers.data.clients.TelegramClient
import com.progcorp.unitedmessengers.data.clients.VKClient
import com.progcorp.unitedmessengers.data.db.TelegramRepository
import com.progcorp.unitedmessengers.data.db.VKRepository
import com.progcorp.unitedmessengers.enums.TelegramAuthStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val telegramClient: TelegramClient,
    val vkClient: VKClient,
    private val vkRepository: VKRepository,
    private val telegramRepository: TelegramRepository
) : ViewModel() {

    val uiState = mutableStateOf<UiState>(UiState.Loading)

    init {
        telegramClient.authState.onEach {
            when (it) {
                TelegramAuthStatus.UNAUTHENTICATED -> {
                    telegramClient.startAuthentication()
                }
                TelegramAuthStatus.WAIT_FOR_NUMBER,
                TelegramAuthStatus.WAIT_FOR_CODE,
                TelegramAuthStatus.WAIT_FOR_PASSWORD -> uiState.value = UiState.Login
                TelegramAuthStatus.AUTHENTICATED -> uiState.value = UiState.Loaded
                TelegramAuthStatus.UNKNOWN -> {
                }
            }
        }.launchIn(viewModelScope)
    }

    val telegramConversations = viewModelScope.async {
        telegramRepository.getConversations().first()
    }.onAwait
    val vkConversations = viewModelScope.async {
        vkRepository.getConversations(0).first()
    }.onAwait

}

sealed class UiState {
    object Loading : UiState()
    object Login : UiState()
    object Loaded : UiState()
}