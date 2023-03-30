package me.progneo.unifychat.domain.model

import com.google.gson.annotations.SerializedName

data class VKChatPhoto(
    @SerializedName("photo_50")
    val photo50: String,
    @SerializedName("photo_100")
    val photo100: String,
    @SerializedName("photo_200")
    val photo200: String,
)
