package me.progneo.unifychat.domain.model

import com.google.gson.annotations.SerializedName

data class VKUser(
    val id: Long,
    @SerializedName("photo_100")
    val photoUrl: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("can_access_closed")
    val canAccessClosed: Boolean,
    @SerializedName("is_closed")
    val isClosed: Boolean,
)
