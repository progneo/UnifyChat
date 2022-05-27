package com.progcorp.unitedmessengers.interfaces.requests

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VKUsersRequest {
    @GET("users.get")
    suspend fun usersGet(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("fields") fields: String,
        @Query("lang") lang: Int
    ): Response<String>
}