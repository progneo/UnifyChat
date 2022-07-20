package com.progcorp.unitedmessengers.interfaces.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VKSendMessageRequest {
    @GET("messages.send")
    suspend fun messageSend(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("peer_id") peer_id: Long,
        @Query("message") message: String,
        @Query("random_id") random_id: Int,
        @Query("reply_to") reply_to: Long,
        @Query("lang") lang: Int
    ): Response<String>
}