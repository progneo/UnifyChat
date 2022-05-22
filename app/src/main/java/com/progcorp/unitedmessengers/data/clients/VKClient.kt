package com.progcorp.unitedmessengers.data.clients

import android.content.SharedPreferences
import com.progcorp.unitedmessengers.interfaces.IAccountService

class VKClient (private val sharedPreference: SharedPreferences) : IAccountService {

    companion object {
        const val SCOPE = "1073737727"
        const val TOKEN = "token"
        const val USER_ID = "userId"
    }

    override var token: String?
        get() {
            return sharedPreference.getString(TOKEN, null)
        }
        set(value) {
            with(sharedPreference.edit()) {
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
            return sharedPreference.getString(USER_ID, null)
        }
        set(value) {
            with(sharedPreference.edit()) {
                if (value == null) {
                    remove(USER_ID)
                }
                else {
                    putString(USER_ID, value)
                }
                apply()
            }
        }
}