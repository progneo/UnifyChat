package me.progneo.unifychat.data.repository.vk

import me.progneo.unifychat.data.model.RequestException
import me.progneo.unifychat.data.service.vk.UsersService
import me.progneo.unifychat.data.model.objects.companions.User
import me.progneo.unifychat.domain.repository.vk.UsersRepository
import me.progneo.unifychat.util.vkParseUser
import org.json.JSONObject
import java.net.HttpURLConnection
import javax.inject.Inject

class UsersRepositoryImpl @Inject constructor(
    private val _usersService: UsersService
) : UsersRepository {

    override suspend fun getUser(token: String): Result<User> {
        val apiResponse = _usersService.getUser(token, "5.131", "photo_100", 0).body()
        if (apiResponse != null) {
            val user = vkParseUser(apiResponse.response[0])
            return Result.success(user)
        }

        return Result.failure(
            RequestException(
                code = HttpURLConnection.HTTP_INTERNAL_ERROR,
                message = "An error occurred!"
            )
        )
    }
}