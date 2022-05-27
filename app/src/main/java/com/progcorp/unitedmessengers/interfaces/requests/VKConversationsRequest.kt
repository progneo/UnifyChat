package com.progcorp.unitedmessengers.interfaces.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VKConversationsRequest {
    @GET("messages.getConversations")
    suspend fun conversationsGet(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("count") count: Int,
        @Query("offset") offset: Int,
        @Query("extended") extended: Boolean,
        @Query("lang") lang: Int
    ): Response<String>
}