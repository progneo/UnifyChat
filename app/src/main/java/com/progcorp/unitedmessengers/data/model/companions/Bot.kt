package com.progcorp.unitedmessengers.data.model.companions

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

        fun tgParse(tdUser: TdApi.User): Bot {
            val id = tdUser.id
            val title = tdUser.firstName
            var photo = ""
            if (tdUser.profilePhoto != null) {
                photo = tdUser.profilePhoto!!.small.id.toString()
            }
            return Bot(id, title, photo, Constants.Messenger.TG)
        }
    }
}