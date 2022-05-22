package com.progcorp.unitedmessengers.data.model

import android.os.Parcel
import android.os.Parcelable
import com.progcorp.unitedmessengers.App
import kotlinx.coroutines.flow.mapNotNull
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONObject

data class Chat(
    val id: Long = 0,
    val title: String = "",
    val photo: String = "",
    val membersCount: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(photo)
        parcel.writeInt(membersCount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Chat> {
        override fun createFromParcel(parcel: Parcel): Chat {
            return Chat(parcel)
        }

        override fun newArray(size: Int): Array<Chat?> {
            return arrayOfNulls(size)
        }

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