package com.progcorp.unitedmessengers.data.model

import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.util.Constants
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONObject

data class Chat(
    override val id: Long = 0,
    var title: String = "",
    override var photo: String = "",
    val membersCount: Int = 0,
    override var messenger: Int = 0
) : ICompanion {
    companion object {
        fun vkParse(json: JSONObject, peerId: Long) = Chat(
            id = peerId,
            title = json.optString("title"),
            photo = json.optJSONObject("photo")?.optString("photo_100") ?: "",
            membersCount = json.optInt("members_count"),
            Constants.Messenger.VK
        )

        suspend fun tgParseSupergroup(tdChat: TdApi.Chat, group: TdApi.Supergroup): Chat {
            val id: Long = group.id
            val title: String = tdChat.title
            val photo = ""
            val membersCount: Int = group.memberCount
            val chat = Chat(id, title, photo, membersCount, Constants.Messenger.TG)
            if (tdChat.photo != null) {
                chat.loadPhoto(tdChat.photo!!.small)
            }
            return chat
        }

       suspend fun tgParseBasicGroup(tdChat: TdApi.Chat, group: TdApi.BasicGroup): Chat {
           val id: Long = group.id
           val title: String = tdChat.title
           val photo = ""
           val membersCount: Int = group.memberCount
           val chat = Chat(id, title, photo, membersCount, Constants.Messenger.TG)
           if (tdChat.photo != null) {
               chat.loadPhoto(tdChat.photo!!.small)
           }
            return chat
        }
    }

    override fun loadPhoto(file: TdApi.File) {
        val client = App.application.tgClient
        MainScope().launch {
            val result = async { client.downloadableFile(file).first() }
            val path = result.await()
            if (path != null) {
                photo = path
            }
        }
    }
}