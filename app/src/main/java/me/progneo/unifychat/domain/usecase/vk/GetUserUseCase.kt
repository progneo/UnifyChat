package me.progneo.unifychat.domain.usecase.vk

import me.progneo.unifychat.data.model.objects.companions.User
import me.progneo.unifychat.domain.repository.vk.UsersRepository
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val _usersRepository: UsersRepository
) {

    suspend fun getUser(token: String): Result<User> {
        return _usersRepository.getUser(token)
    }
}