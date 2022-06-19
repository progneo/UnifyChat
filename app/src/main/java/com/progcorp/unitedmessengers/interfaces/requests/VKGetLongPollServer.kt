package com.progcorp.unitedmessengers.interfaces.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VKGetLongPollServer {
    @GET("messages.getLongPollServer")
    suspend fun messagesLongPollServerGet(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("need_pts") need_pts: Boolean,
        @Query("lp_version") lp_version: String
    ): Response<String>
}