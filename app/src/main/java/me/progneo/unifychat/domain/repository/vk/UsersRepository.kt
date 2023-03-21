package me.progneo.unifychat.domain.repository.vk

import me.progneo.unifychat.data.model.objects.companions.User

interface UsersRepository {

    suspend fun getUser(token: String): Result<User>
}