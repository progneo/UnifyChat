package com.progcorp.unitedmessengers.data.model.companions

import com.progcorp.unitedmessengers.interfaces.ICompanion
import com.progcorp.unitedmessengers.util.Constants
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
            photo = json.optString("photo_100") ?: "",
            Constants.Messenger.VK
        )

        fun tgParse(tdUser: TdApi.User): Bot {
            val id = tdUser.id
            val title = tdUser.firstName
            var photo = ""
            if (tdUser.profilePhoto != null) {
                photo = if (tdUser.profilePhoto!!.small.local.isDownloadingCompleted){
                    tdUser.profilePhoto!!.small.local.path
                } else {
                    tdUser.profilePhoto!!.small.id.toString()
                }
            }
            return Bot(id, title, photo, Constants.Messenger.TG)
        }
    }

    override fun getName(): String {
        return title
    }
}