package com.progcorp.unitedmessengers.interfaces.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VKMarkAsRead {
    @GET("messages.markAsRead")
    suspend fun markAsRead(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("peer_id") peer_id: Long,
        @Query("start_message_id") start_message_id: Long,
        @Query("lang") lang: Int
    ): Response<String>
}