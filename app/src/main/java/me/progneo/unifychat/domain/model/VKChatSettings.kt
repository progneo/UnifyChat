package me.progneo.unifychat.domain.model

import com.google.gson.annotations.SerializedName

data class VKChatSettings(
    @SerializedName("members_count")
    val membersCount: Int,
    val title: String,
    val state: String,
    val photo: VKChatPhoto,
)
