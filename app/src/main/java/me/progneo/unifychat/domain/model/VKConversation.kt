package me.progneo.unifychat.domain.model

import com.google.gson.annotations.SerializedName

data class VKConversation(
    val peer: VKPeer,
    @SerializedName("last_message_id")
    val lastMessageId: Int,
    @SerializedName("in_read")
    val inRead: Int,
    @SerializedName("out_read")
    val outRead: Int,
    @SerializedName("unread_count")
    val unreadCount: Int,
    @SerializedName("important")
    val isImportant: Boolean,
    @SerializedName("can_write")
    val canWrite: VKCanWrite,
    val style: String,
)
