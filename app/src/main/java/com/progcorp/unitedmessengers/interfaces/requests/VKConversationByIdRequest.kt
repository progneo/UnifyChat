package com.progcorp.unitedmessengers.interfaces.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VKConversationByIdRequest {
    @GET("messages.getConversationsById")
    suspend fun conversationGet(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("peer_ids") peer_ids: Long,
        @Query("extended") extended: Boolean,
        @Query("lang") lang: Int
    ): Response<String>
}