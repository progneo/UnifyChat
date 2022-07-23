package com.progcorp.unitedmessengers.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.progcorp.unitedmessengers.App

class MainViewModel : ViewModel() {
    private val _vkClient = App.application.vkClient
    private val _tgClient = App.application.tgClient

    val vkUnreadCount: LiveData<Int?> = _vkClient.unreadCount
    val tgUnreadCount: LiveData<Int?> = _tgClient.unreadCount
}