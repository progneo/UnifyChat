package com.progcorp.unitedmessengers.data.model

import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.util.Constants
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONObject

data class User(
    override val id: Long = 0,
    var firstName: String = "",
    var lastName: String = "",
    override var photo: String = "",
    var lastSeen: Long = 0,
    var isOnline: Boolean = false,
    var deactivated: Boolean = false
) : ICompanion {

    companion object {
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
            val client = App.application.tgClient

            val id = tdUser.id
            val firstName = tdUser.firstName
            val lastName = tdUser.lastName
            var photo = "https://www.meme-arsenal.com/memes/8b6f5f94a53dbc3c8240347693830120.jpg"
            if (tdUser.profilePhoto != null) {
                client.downloadableFile(tdUser.profilePhoto!!.small).mapNotNull {
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