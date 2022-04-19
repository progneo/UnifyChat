package com.progcorp.unitedmessengers.data.db.vk.requests

import com.progcorp.unitedmessengers.data.model.Message
import com.vk.api.sdk.VKApiJSONResponseParser
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject

class VKMessagesCommand(private val id: Int, private val offset: Int, private val count: Int): ApiCommand<List<Message>>() {
    override fun onExecute(manager: VKApiManager): List<Message> {
        val result = ArrayList<Message>()
        val call = VKMethodCall.Builder()
            .method("messages.getHistory")
            .args("offset", offset)
            .args("count", count)
            .args("peer_id", id)
            .args("extended", true)
            .args("lang", 0)
            .version(manager.config.version)
            .build()
        result.addAll(manager.execute(call, ResponseApiParser()))

        return result
    }

    private class ResponseApiParser : VKApiJSONResponseParser<List<Message>> {
        override fun parse(responseJson: JSONObject): List<Message> {
            try {
                val o = responseJson.getJSONObject("response").getJSONArray("items")
                val p = responseJson.getJSONObject("response").optJSONArray("profiles")
                val r = ArrayList<Message>(o.length())
                for (i in 0 until o.length()) {
                    val message = Message.parseVK(
                        o.getJSONObject(i),
                        p
                    )
                    r.add(message)
                }
                return r
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }
}