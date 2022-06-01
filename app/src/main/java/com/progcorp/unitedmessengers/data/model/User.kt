package com.progcorp.unitedmessengers.data.model

import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.util.Constants
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONObject

data class User(
    override val id: Long = 0,
    var firstName: String = "",
    var lastName: String = "",
    override var photo: String = "",
    var lastSeen: Long = 0,
    var isOnline: Boolean = false,
    var deactivated: Boolean = false,
    override var messenger: Int = 0
) : ICompanion {

    companion object {
        fun vkParse(json: JSONObject) = User(
            id = json.optLong("id", 0),
            firstName = json.optString("first_name", ""),
            lastName = json.optString("last_name", ""),
            photo = json.optString("photo_100", ""),
            lastSeen = (json.optJSONObject("online_info")?.optLong("last_seen")
                ?: json.optJSONObject("last_seen")?.optLong("time") ?: 0) * 1000,
            isOnline = json.optInt("online") != 0,
            deactivated = json.optBoolean("deactivated", false),
            Constants.Messenger.VK
        )

        suspend fun tgParse(tdUser: TdApi.User): User {
            val id = tdUser.id
            val firstName = tdUser.firstName
            val lastName = tdUser.lastName
            var isOnline = false
            val lastSeen: Long
            val photo = ""
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
            val user = User(id, firstName, lastName, photo, lastSeen, isOnline, deactivated, Constants.Messenger.TG)
            if (tdUser.profilePhoto != null) {
                user.loadPhoto(tdUser.profilePhoto!!.small)
            }
            return user
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