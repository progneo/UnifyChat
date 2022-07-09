package com.progcorp.unitedmessengers.interfaces.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VKGetLongPollHistory {
    @GET("messages.getLongPollHistory")
    suspend fun messagesLongPollHistoryGet(): Response<String>
}