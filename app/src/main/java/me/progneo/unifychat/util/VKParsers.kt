package me.progneo.unifychat.util

import me.progneo.unifychat.data.model.objects.companions.User
import me.progneo.unifychat.domain.model.VKUser

fun vkParseUser(user: VKUser) = User(
    id = user.id,
    firstName = user.firstName,
    lastName = user.lastName,
    photo = user.photoUrl,
    messenger = Messengers.VK,
)
