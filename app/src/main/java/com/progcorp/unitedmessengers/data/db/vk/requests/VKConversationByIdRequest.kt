package com.progcorp.unitedmessengers.data.db.vk.requests

import retrofit2.http.GET
import retrofit2.http.Query

interface VKConversationByIdRequest {
    @GET("messages.getConversationsById")
    suspend fun conversationGet(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("peer_ids") peer_ids: Int,
        @Query("extended") extended: Boolean,
        @Query("lang") lang: Int
    ): String
}