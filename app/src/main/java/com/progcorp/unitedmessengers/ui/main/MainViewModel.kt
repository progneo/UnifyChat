package com.progcorp.unitedmessengers.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.db.Users
import com.progcorp.unitedmessengers.data.model.User
import kotlinx.coroutines.launch

class MainViewModel : ViewModel(), Users.OnUsersFetched {
    private val _users: Users = Users(this)
    private val _vkUserInfo: MutableLiveData<User> = MutableLiveData()

    var vkUserInfo: LiveData<User> = _vkUserInfo

    init {
        viewModelScope.launch {
            if (App.application.vkAccountService.token != null) {
                setUserInfo()
            }
        }
    }

    override fun showUsers(users: ArrayList<User>) {
        _vkUserInfo.value = users[0]
    }

    private fun setUserInfo() {
        _users.vkGetUsers(intArrayOf(App.application.vkAccountService.userId!!.toInt()))
    }
}