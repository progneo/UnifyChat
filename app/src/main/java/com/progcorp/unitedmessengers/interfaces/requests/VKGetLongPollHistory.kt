package com.progcorp.unitedmessengers.interfaces.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VKGetLongPollHistory {
    @GET("{server}?act=a_check")
    suspend fun messagesLongPollHistoryGet(
        @Path("server") server: String,
        @Query("key") key: String,
        @Query("ts") ts: Long,
        @Query("pts") pts: Long,
        @Query("wait") wait: Int,
        @Query("mode") mode: Int,
        @Query("version") version: String,
        @Query("extended") extended: Boolean
    ): Response<String>
}