package com.progcorp.unitedmessengers.data.model.companions

import androidx.databinding.BaseObservable
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.util.Constants
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONObject

data class Chat(
    override val id: Long = 0,
    var title: String = "",
    override var photo: String = "",
    val membersCount: Int = 0,
    override var messenger: Int = 0
) : ICompanion, BaseObservable() {
    companion object {
        fun vkParse(json: JSONObject, peerId: Long) = Chat(
            id = peerId,
            title = json.optString("title"),
            photo = json.optJSONObject("photo")?.optString("photo_100") ?: "",
            membersCount = json.optInt("members_count"),
            Constants.Messenger.VK
        )

        fun tgParseSupergroup(tdChat: TdApi.Chat, group: TdApi.Supergroup): Chat {
            val id: Long = group.id
            val title: String = tdChat.title
            val membersCount: Int = group.memberCount
            val photo: String = if (tdChat.photo != null) {
                if (tdChat.photo!!.small.local.isDownloadingCompleted){
                    tdChat.photo!!.small.local.path
                } else {
                    tdChat.photo!!.small.id.toString()
                }
            }
            else {
                ""
            }

            return Chat(id, title, photo, membersCount, Constants.Messenger.TG)
        }

        fun tgParseBasicGroup(tdChat: TdApi.Chat, group: TdApi.BasicGroup): Chat {
            val id: Long = group.id
            val title: String = tdChat.title
            val membersCount: Int = group.memberCount
            val photo: String = if (tdChat.photo != null) {
                if (tdChat.photo!!.small.local.isDownloadingCompleted){
                    tdChat.photo!!.small.local.path
                } else {
                    tdChat.photo!!.small.id.toString()
                }
            }
            else {
                ""
            }
            return Chat(id, title, photo, membersCount, Constants.Messenger.TG)
        }
    }

    override fun getName(): String {
        return title
    }
}