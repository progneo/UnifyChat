package com.progcorp.unitedmessengers.data.db.vk.requests

import retrofit2.http.GET
import retrofit2.http.Query

interface VKUsersRequest {
    @GET("users.get")
    suspend fun usersGet(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("user_ids") user_ids: String,
        @Query("fields") fields: String,
        @Query("lang") lang: Int
    ): String
}