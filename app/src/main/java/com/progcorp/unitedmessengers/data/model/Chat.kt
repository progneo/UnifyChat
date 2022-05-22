package com.progcorp.unitedmessengers.data.model

import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.interfaces.ICompanion
import kotlinx.coroutines.flow.mapNotNull
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONObject

data class Chat(
    val id: Long = 0,
    val title: String = "",
    val photo: String = "",
    val membersCount: Int = 0
) : ICompanion {
    companion object {
        fun vkParse(json: JSONObject, peerId: Long) = Chat(
            id = peerId,
            title = json.optString("title"),
            photo = json.optJSONObject("photo")?.optString("photo_100")
                ?: "https://www.meme-arsenal.com/memes/8b6f5f94a53dbc3c8240347693830120.jpg",
            membersCount = json.optInt("members_count")
        )

        fun tgParseSupergroup(chat: TdApi.Chat, group: TdApi.Supergroup): Chat {
            val tgClient = App.application.tgClient

            val id: Long = group.id
            val title: String = chat.title
            var photo: String = "https://www.meme-arsenal.com/memes/8b6f5f94a53dbc3c8240347693830120.jpg"
            if (chat.photo != null) {
                tgClient.downloadableFile(chat.photo!!.small).mapNotNull {
                    photo = it!!
                }
            }
            val membersCount: Int = group.memberCount

            return Chat(id, title, photo, membersCount)
        }

        fun tgParseBasicGroup(chat: TdApi.Chat, group: TdApi.BasicGroup): Chat {
            val tgClient = App.application.tgClient

            val id: Long = group.id
            val title: String = chat.title
            var photo: String = "https://www.meme-arsenal.com/memes/8b6f5f94a53dbc3c8240347693830120.jpg"
            if (chat.photo != null) {
                tgClient.downloadableFile(chat.photo!!.small).mapNotNull {
                    photo = it!!
                }
            }
            val membersCount: Int = group.memberCount

            return Chat(id, title, photo, membersCount)
        }
    }
}