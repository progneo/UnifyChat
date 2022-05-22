package com.progcorp.unitedmessengers.data.model

import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.util.Constants
import kotlinx.coroutines.flow.mapNotNull
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONObject

data class Bot(
    val id: Long = 0,
    var title: String = "",
    var photo: String = "",
) : ICompanion {
    companion object {
        fun vkParse(json: JSONObject) = Bot(
            id = json.optLong("id"),
            title = json.optString("name"),
            photo = json.optJSONObject("photo")?.optString("photo_100")
                ?: "https://www.meme-arsenal.com/memes/8b6f5f94a53dbc3c8240347693830120.jpg"
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