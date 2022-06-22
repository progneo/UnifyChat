package com.progcorp.unitedmessengers.data.clients

import android.content.SharedPreferences
import com.progcorp.unitedmessengers.data.model.VKLongPollServer
import com.progcorp.unitedmessengers.interfaces.IAccountService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class VKClient (
        private val _sharedPreference: SharedPreferences,
        private var _longPollServer: VKLongPollServer,
        private var _longPollRetrofit: Retrofit
    ) : IAccountService {

    override var token: String?
        get() {
            return _sharedPreference.getString(TOKEN, null)
        }
        set(value) {
            with(_sharedPreference.edit()) {
                if (value == null) {
                    remove(TOKEN)
                }
                else {
                    putString(TOKEN, value)
                }
                apply()
            }
        }

    override var userId: String?
        get() {
            return _sharedPreference.getString(USER_ID, null)
        }
        set(value) {
            with(_sharedPreference.edit()) {
                if (value == null) {
                    remove(USER_ID)
                }
                else {
                    putString(USER_ID, value)
                }
                apply()
            }
        }

    companion object {
        const val SCOPE = "1073737727"
        const val TOKEN = "token"
        const val USER_ID = "userId"
    }
}