package com.progcorp.unitedmessengers.data.db.vk.requests

import retrofit2.http.GET
import retrofit2.http.Query

interface VKSendMessageRequest {
    @GET("messages.send")
    suspend fun messageSend(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("peer_id") peer_id: Int,
        @Query("message") message: String,
        @Query("random_id") random_id: Int,
        @Query("lang") lang: Int
    ): String
}