package com.progcorp.unitedmessengers.data.model

import android.os.Parcel
import android.os.Parcelable
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.util.Constants
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONObject

data class User(
    val id: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val photo: String = "",
    val lastSeen: Long = 0,
    val isOnline: Boolean = false,
    val deactivated: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(photo)
        parcel.writeLong(lastSeen)
        parcel.writeByte(if (isOnline) 1 else 0)
        parcel.writeByte(if (deactivated) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }

        fun vkParse(json: JSONObject) = User(
            id = json.optLong("id", 0),
            firstName = json.optString("first_name", ""),
            lastName = json.optString("last_name", ""),
            photo = json.optString("photo_100", ""),
            lastSeen = (json.optJSONObject("last_seen")?.optLong("time") ?: 0) * 1000,
            isOnline = json.optInt("online") != 0,
            deactivated = json.optBoolean("deactivated", false)
        )

        fun tgParse(tdUser: TdApi.User): User {
            val tgClient = App.application.tgClient

            val id = tdUser.id
            val firstName = tdUser.firstName
            val lastName = tdUser.lastName
            var photo = "https://www.meme-arsenal.com/memes/8b6f5f94a53dbc3c8240347693830120.jpg"
            if (tdUser.profilePhoto != null) {
                tgClient.downloadableFile(tdUser.profilePhoto!!.small).mapNotNull {
                    photo = it!!
                }
            }
            var isOnline = false
            val lastSeen: Long
            when (tdUser.status.constructor) {
                TdApi.UserStatusEmpty.CONSTRUCTOR -> {
                    lastSeen = Constants.LastSeen.unknown
                }
                TdApi.UserStatusLastMonth.CONSTRUCTOR -> {
                    lastSeen = Constants.LastSeen.lastMonth
                }
                TdApi.UserStatusLastWeek.CONSTRUCTOR -> {
                    lastSeen = Constants.LastSeen.lastWeek
                }
                TdApi.UserStatusOffline.CONSTRUCTOR -> {
                    lastSeen = ((tdUser.status as TdApi.UserStatusOffline).wasOnline).toLong() * 1000
                }
                TdApi.UserStatusOnline.CONSTRUCTOR -> {
                    lastSeen = Constants.LastSeen.unknown
                    isOnline = true
                }
                TdApi.UserStatusRecently.CONSTRUCTOR -> {
                    lastSeen = Constants.LastSeen.recently
                }
                else -> lastSeen = Constants.LastSeen.unknown
            }
            val deactivated = !tdUser.haveAccess
            return User(id, firstName, lastName, photo, lastSeen, isOnline, deactivated)
        }
    }
}