package me.progneo.unifychat.data.model.clients

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.progneo.unifychat.data.enums.VKAuthStatus
import me.progneo.unifychat.data.model.objects.companions.User
import me.progneo.unifychat.domain.usecase.vk.GetUserUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VKClient @Inject constructor(
    private val _sharedPreferences: SharedPreferences,
    private val _getUserUseCase: GetUserUseCase
) {

    private val _authStatus = MutableLiveData<VKAuthStatus>()
    val authStatus: LiveData<VKAuthStatus> = _authStatus

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    init {
        _authStatus.value = if (token != null) {
            getUser()
            VKAuthStatus.SUCCESS
        } else {
            VKAuthStatus.AUTH
        }
    }

    private fun getUser() {
        MainScope().launch {
            _getUserUseCase.getUser(token!!)
                .onSuccess {
                    _user.value = it
                }
                .onFailure {
                    token = null
                    _authStatus.value = VKAuthStatus.AUTH
                }
        }
    }

    fun login(token: String) {
        this.token = token
    }

    fun logout() {
        token = null;
        _user.value = null;
        _authStatus.value = VKAuthStatus.AUTH
    }

    var token: String?
        get() {
            return _sharedPreferences.getString(TOKEN, null)
        }
        private set(value) {
            with(_sharedPreferences.edit()) {
                if (value == null) {
                    remove(TOKEN)
                }
                else {
                    putString(TOKEN, value)
                    getUser()
                }
                apply()
            }

        }

    companion object {
        const val SCOPE = "1073737727"
        const val TOKEN = "token"
    }
}