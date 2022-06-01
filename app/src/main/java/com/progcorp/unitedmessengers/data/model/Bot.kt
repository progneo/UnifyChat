package com.progcorp.unitedmessengers.data.model

import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.util.Constants
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONObject

data class Bot(
    override val id: Long = 0,
    var title: String = "",
    override var photo: String = "",
    override var messenger: Int = 0
) : ICompanion {
    companion object {
        fun vkParse(json: JSONObject) = Bot(
            id = json.optLong("id"),
            title = json.optString("name"),
            photo = json.optJSONObject("photo")?.optString("photo_100") ?: "",
            Constants.Messenger.VK
        )

        suspend fun tgParse(tdUser: TdApi.User): Bot {
            val id = tdUser.id
            val title = tdUser.firstName
            val photo = ""
            val bot = Bot(id, title, photo, Constants.Messenger.TG)
            if (tdUser.profilePhoto != null) {
                bot.loadPhoto(tdUser.profilePhoto!!.small)
            }
            return bot
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