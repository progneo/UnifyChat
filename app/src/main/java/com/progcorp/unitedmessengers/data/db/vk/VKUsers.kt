package com.progcorp.unitedmessengers.data.db.vk

import android.util.Log
import com.progcorp.unitedmessengers.data.db.vk.requests.VKUsersCommand
import com.progcorp.unitedmessengers.data.model.User

import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback

class VKUsers(private val onUsersFetched: OnUsersFetched) {
    fun getUsers(uids: IntArray) {
        VK.execute(VKUsersCommand(uids), object : VKApiCallback<List<User>> {
            override fun success(result: List<User>) {
                onUsersFetched.showUsers(result as ArrayList<User>)
            }

            override fun fail(error: Exception) {
                Log.e(TAG, error.toString())
                onUsersFetched.showUsers(arrayListOf<User>())
            }
        })
    }

    interface OnUsersFetched {
        fun showUsers(users: ArrayList<User>)
    }

    companion object {
        const val TAG = "VKUsers"
    }
}