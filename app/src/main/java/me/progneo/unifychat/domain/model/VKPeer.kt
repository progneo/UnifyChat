package me.progneo.unifychat.domain.model

import com.google.gson.annotations.SerializedName

data class VKPeer(
    val id: Int,
    val type: String,
    @SerializedName("local_id")
    val localId: Int,
)
