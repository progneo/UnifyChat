package com.progcorp.unitedmessengers.data.db.vk.requests

import com.progcorp.unitedmessengers.data.model.Conversation
import com.vk.api.sdk.VKApiJSONResponseParser
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject

class VKChatsCommand(private val offset: Int): ApiCommand<List<Conversation>>(){
    override fun onExecute(manager: VKApiManager): List<Conversation> {
        val result = ArrayList<Conversation>()
        val call = VKMethodCall.Builder()
            .method("messages.getConversations")
            .args("count", 30)
            .args("offset", offset)
            .args("filter", "all")
            .args("extended", true)
            .args("lang", 0)
            .version(manager.config.version)
            .build()
        result.addAll(manager.execute(call, ResponseApiParser()))
        return result
    }


    private class ResponseApiParser : VKApiJSONResponseParser<List<Conversation>> {
        override fun parse(responseJson: JSONObject): List<Conversation> {
            try {
                val o = responseJson.getJSONObject("response").getJSONArray("items")
                val r = ArrayList<Conversation>(o.length())
                for (i in 0 until o.length()) {
                    val chat = Conversation.parse(
                        o.getJSONObject(i),
                        responseJson.getJSONObject("response").optJSONArray("profiles"),
                        responseJson.getJSONObject("response").optJSONArray("groups")
                    )
                    r.add(chat)
                }
                return r
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }
}
