package me.progneo.unifychat.domain.model

data class VKUser(
    val id: Long,
    val photo_100: String,
    val first_name: String,
    val last_name: String,
    val can_access_closed: Boolean,
    val is_closed: Boolean
)