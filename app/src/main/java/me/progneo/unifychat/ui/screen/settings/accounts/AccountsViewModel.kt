package me.progneo.unifychat.ui.screen.settings.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.progneo.unifychat.data.model.clients.VKClient
import javax.inject.Inject

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val _vkClient: VKClient,
) : ViewModel() {

    private val _accountUiState = MutableStateFlow(AccountUiState())
    val accountUiState: StateFlow<AccountUiState> = _accountUiState.asStateFlow()

    fun collectAccountsState() {
        viewModelScope.launch {
            _vkClient.user.collect { user ->
                _accountUiState.update {
                    it.copy(
                        isVkConnected = user != null,
                        vkUsername = user?.getName() ?: ""
                    )
                }
            }
        }
    }

    fun hideLogoutDialog() {
        _accountUiState.update { it.copy(logoutDialogVisible = false) }
    }

    fun showLogoutDialog() {
        _accountUiState.update { it.copy(logoutDialogVisible = true) }
    }

    fun logoutVk() {
        _vkClient.logout()
    }
}

data class AccountUiState(
    val isVkConnected: Boolean = false,
    val vkUsername: String = "",
    val isTelegramConnected: Boolean = false,
    val telegramUsername: String = "",
    val logoutDialogVisible: Boolean = false,
)