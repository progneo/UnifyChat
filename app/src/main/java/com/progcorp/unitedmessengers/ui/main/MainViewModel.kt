package com.progcorp.unitedmessengers.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.progcorp.unitedmessengers.data.db.vk.VKUsers
import com.progcorp.unitedmessengers.data.model.User
import com.vk.api.sdk.VK

class MainViewModel : ViewModel(), VKUsers.OnUsersFetched {
    private val _users: VKUsers = VKUsers(this)
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
        _users.getUsers(intArrayOf(VK.getUserId().toString().toInt()))
    }
}