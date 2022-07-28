package com.progcorp.unitedmessengers.interfaces.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VKUsersRequest {
    @GET("users.get")
    suspend fun userGet(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("fields") fields: String,
        @Query("lang") lang: Int
    ): Response<String>

    @GET("users.get")
    suspend fun usersGet(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("user_ids") user_ids: String,
        @Query("fields") fields: String,
        @Query("lang") lang: Int
    ): Response<String>
}