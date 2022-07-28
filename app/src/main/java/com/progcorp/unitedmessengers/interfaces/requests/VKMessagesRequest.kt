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
        @Query("peer_id") peer_id: Long,
        @Query("extended") extended: Boolean,
        @Query("lang") lang: Int
    ): Response<String>

    @GET("messages.getHistory")
    suspend fun messagesGetFromId(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("count") count: Int,
        @Query("start_message_id") start_message_id: Long,
        @Query("peer_id") peer_id: Long,
        @Query("extended") extended: Boolean,
        @Query("lang") lang: Int
    ): Response<String>

    @GET("messages.getById")
    suspend fun messagesGetById(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("message_ids") message_ids: String,
        @Query("extended") extended: Boolean,
        @Query("lang") lang: Int
    ): Response<String>
}