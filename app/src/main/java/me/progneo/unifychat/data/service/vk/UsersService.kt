package me.progneo.unifychat.data.service.vk

import me.progneo.unifychat.data.model.response.vk.UsersResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UsersService {

    @GET("users.get")
    suspend fun getUser(
        @Query("access_token") token: String,
        @Query("v") v: String,
        @Query("fields") fields: String,
        @Query("lang") lang: Int,
    ): Response<UsersResponse>
}
