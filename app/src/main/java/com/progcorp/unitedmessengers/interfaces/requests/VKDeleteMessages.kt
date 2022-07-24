package com.progcorp.unitedmessengers.interfaces.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VKDeleteMessages {
    @GET("messages.delete")
    suspend fun deleteMessages(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("message_ids") message_ids: String
    ): Response<String>

    @GET("messages.delete")
    suspend fun deleteMessagesForAll(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("message_ids") message_ids: String,
        @Query("delete_for_all") delete_for_all: Boolean
    ): Response<String>
}