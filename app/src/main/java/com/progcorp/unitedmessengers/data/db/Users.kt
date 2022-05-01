package com.progcorp.unitedmessengers.data.db

import android.util.Log
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.db.vk.requests.VKUsersRequest
import com.progcorp.unitedmessengers.data.model.User
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.json.JSONObject

class Users(private val onUsersFetched: OnUsersFetched) {
    fun vkGetUsers(uids: IntArray) = runBlocking {
        val chunks = uids.toList().chunked(CHUNK_LIMIT)
        val response = App.application.vkRetrofit.create(VKUsersRequest::class.java)
            .usersGet(
                App.application.vkAccountService.token!!, "5.131", "", "photo_100,online,last_seen", 0
            )
        val responseJson = JSONObject(response)
        try {
            val ja = responseJson.getJSONArray("response")
            val r = ArrayList<User>(ja.length())
            for (i in 0 until ja.length()) {
                val user = User.parse(ja.getJSONObject(i))
                r.add(user)
            }
            onUsersFetched.showUsers(r)
        } catch (ex: JSONException) {
            Log.e(ConversationViewModel.TAG, ex.stackTraceToString())
        }
    }

    interface OnUsersFetched {
        fun showUsers(users: ArrayList<User>)
    }

    companion object {
        const val TAG = "Users"
        const val CHUNK_LIMIT = 900
    }
}