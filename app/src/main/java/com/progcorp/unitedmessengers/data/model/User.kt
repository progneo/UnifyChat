package com.progcorp.unitedmessengers.data.model

import android.os.Parcel
import android.os.Parcelable
import com.progcorp.unitedmessengers.App
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONObject

data class User(
    val id: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val photo: String = "",
    val lastSeen: Long = 0,
    val isOnline: Int = 0,
    val deactivated: Boolean = false) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(photo)
        parcel.writeLong(lastSeen)
        parcel.writeByte(if (deactivated) 1 else 0)
        parcel.writeInt(isOnline)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Conversation> {
        override fun createFromParcel(parcel: Parcel): Conversation {
            return Conversation(parcel)
        }

        override fun newArray(size: Int): Array<Conversation?> {
            return arrayOfNulls(size)
        }

        fun vkParse(json: JSONObject)
                = User(id = json.optLong("id", 0),
            firstName = json.optString("first_name", ""),
            lastName = json.optString("last_name", ""),
            photo = json.optString("photo_100", ""),
            lastSeen = json.optJSONObject("last_seen")?.optLong("time") ?: 0,
            isOnline = json.optInt("online"),
            deactivated = json.optBoolean("deactivated", false))

        fun tgParse(tdUser: TdApi.User): User {
            val id = tdUser.id
            val firstName = tdUser.firstName
            val lastName = tdUser.lastName
            val photo = "https://www.meme-arsenal.com/memes/8b6f5f94a53dbc3c8240347693830120.jpg"
            val lastSeen: Long = 0
            val isOnline = 0
            val deactivated = !tdUser.haveAccess
            return User(id, firstName, lastName, photo, lastSeen, isOnline, deactivated)
        }
    }
}