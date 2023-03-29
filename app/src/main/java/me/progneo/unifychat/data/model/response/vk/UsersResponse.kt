package me.progneo.unifychat.data.model.response.vk

import me.progneo.unifychat.domain.model.VKUser

data class UsersResponse(
    val response: List<VKUser>,
)
