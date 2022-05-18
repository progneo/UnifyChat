package com.progcorp.unitedmessengers.data.db

import android.util.Log
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.db.telegram.TgUserRepository
import com.progcorp.unitedmessengers.interfaces.requests.VKUsersRequest
import com.progcorp.unitedmessengers.data.model.User
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception

@ExperimentalCoroutinesApi
class Users(private val onUsersFetched: OnUsersFetched) {


    suspend fun tgGetUser(userId: Long) {
        try {
            val response = TgUserRepository().getUser(userId)
            val tdUser = response.first()
            val user = User.tgParse(tdUser)
            onUsersFetched.showUsers(arrayListOf(user))
        }
        catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
        }
    }

    interface OnUsersFetched {
        fun showUsers(users: ArrayList<User>)
    }

    companion object {
        const val TAG = "Users"
    }
}