package com.progcorp.unitedmessengers.interfaces.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VKEditMessage {
    @GET("messages.edit")
    suspend fun editMessage(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("peer_id") peer_id: Long,
        @Query("message") message: String,
        @Query("keep_forward_messages") keep_forward_messages: Boolean,
        @Query("keep_snippets") keep_snippets: Boolean,
        @Query("disable_mentions") disable_mentions: Boolean,
        @Query("message_id") message_id: String,
    ): Response<String>
}