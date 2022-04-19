package com.progcorp.unitedmessengers.data.db.vk.requests

import com.progcorp.unitedmessengers.data.model.Message
import com.vk.api.sdk.VKApiJSONResponseParser
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject

class VKLongPollHistory(private val ts: Int, private val pts: Int): ApiCommand<List<Message>>() {
    override fun onExecute(manager: VKApiManager): List<Message> {
        val result = ArrayList<Message>()
        val call = VKMethodCall.Builder()
            .method("messages.getLongPollHistory")
            .args("ts", ts)
            .args("pts", pts)
            .args("lang", 0)
            .version(manager.config.version)
            .build()
        result.addAll(manager.execute(call, ResponseApiParser()))

        return result
    }

    private class ResponseApiParser : VKApiJSONResponseParser<List<Message>> {
        override fun parse(responseJson: JSONObject): List<Message> {
            try {
                val r = arrayListOf<Message>()
                val o = responseJson.getJSONObject("response")
                    .getJSONObject("messages").optJSONArray("items")
                val p = responseJson.getJSONObject("response").optJSONArray("profiles")
                if (o != null) {
                    for (i in 0 until o.length()) {
                        val message = Message.parseVK(
                            o.getJSONObject(i),
                            p
                        )
                        r.add(message)
                    }
                }
                return r
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }
}