package com.progcorp.unitedmessengers.ui.telegram

import androidx.lifecycle.ViewModel
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.ApiResult
import com.progcorp.unitedmessengers.data.model.Conversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class TelegramViewModel : ViewModel() {
    private val repository = App.application.tgRepository

    private val _conversationsList: Flow<List<Conversation>> = flowOf()

    init {

    }
}