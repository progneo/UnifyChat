package com.progcorp.unitedmessengers.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.progcorp.unitedmessengers.data.db.vk.Users
import com.progcorp.unitedmessengers.data.model.User
import com.vk.api.sdk.VK

class MainViewModel : ViewModel(), Users.OnUsersFetched {
    private val _users: Users = Users(this)
    private val _vkUserInfo: MutableLiveData<User> = MutableLiveData()

    var vkUserInfo: LiveData<User> = _vkUserInfo

    init {

        if (VK.isLoggedIn()) {
            setUserInfo()
        }
    }

    override fun showUsers(users: ArrayList<User>) {
        _vkUserInfo.value = users[0]
    }

    private fun setUserInfo() {
        _users.vkGetUsers(intArrayOf(VK.getUserId().toString().toInt()))
    }
}