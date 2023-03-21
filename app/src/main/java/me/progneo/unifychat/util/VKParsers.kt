package me.progneo.unifychat.util

import me.progneo.unifychat.data.model.objects.companions.User
import me.progneo.unifychat.domain.model.VKUser

fun vkParseUser(user: VKUser) = User(
    id = user.id,
    firstName = user.first_name,
    lastName = user.last_name,
    photo = user.photo_100,
    messenger = Messengers.VK
)