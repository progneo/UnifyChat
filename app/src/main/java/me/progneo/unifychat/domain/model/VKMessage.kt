package me.progneo.unifychat.domain.model

import com.google.gson.annotations.SerializedName

data class VKMessage(
    val date: Long,
    @SerializedName("from_id")
    val fromId: Int,
    val id: Int,
    val out: Int,
    @SerializedName("conversation_message_id")
    val conversationMessageId: Int,
    val important: Boolean,
    @SerializedName("is_hidden")
    val isHidden: Boolean,
    @SerializedName("peer_id")
    val peerId: Int,
    @SerializedName("random_id")
    val randomId: Int,
    val text: String,
)
