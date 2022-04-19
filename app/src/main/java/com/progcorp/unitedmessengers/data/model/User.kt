package com.progcorp.unitedmessengers.data.model

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class User(
    val id: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val photo: String = "",
    val lastSeen: Long = 0,
    val isOnline: Int = 0,
    val deactivated: Boolean = false) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
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

        fun parse(json: JSONObject)
                = User(id = json.optInt("id", 0),
            firstName = json.optString("first_name", ""),
            lastName = json.optString("last_name", ""),
            photo = json.optString("photo_100", ""),
            lastSeen = json.optJSONObject("last_seen")?.optLong("time") ?: 0,
            isOnline = json.optInt("online"),
            deactivated = json.optBoolean("deactivated", false))
    }
}