package com.progcorp.unitedmessengers.interfaces.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VKMessagesRequest {
    @GET("messages.getHistory")
    suspend fun messagesGet(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("count") count: Int,
        @Query("offset") offset: Int,
        @Query("peer_id") peer_id: Long,
        @Query("extended") extended: Boolean,
        @Query("lang") lang: Int
    ): Response<String>
}