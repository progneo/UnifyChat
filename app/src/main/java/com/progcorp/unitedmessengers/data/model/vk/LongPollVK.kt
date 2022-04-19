package com.progcorp.unitedmessengers.data.model.vk

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class LongPollVK(
    val server: String = "",
    val key: String = "",
    val ts: Int = 0,
    val pts: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(server)
        parcel.writeString(key)
        parcel.writeInt(ts)
        parcel.writeInt(pts)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LongPollVK> {
        override fun createFromParcel(parcel: Parcel): LongPollVK {
            return LongPollVK(parcel)
        }

        override fun newArray(size: Int): Array<LongPollVK?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject) = LongPollVK(
            server = json.getString("server"),
            key = json.getString("key"),
            ts = json.getInt("ts"),
            pts = json.getInt("pts")
        )
    }
}
