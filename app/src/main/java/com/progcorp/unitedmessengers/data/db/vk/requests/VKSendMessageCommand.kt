package com.progcorp.unitedmessengers.data.db.vk.requests

import com.vk.api.sdk.VKApiJSONResponseParser
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject

class VKSendMessageCommand(private val peerId: Int, private val text: String): ApiCommand<Int>() {
    override fun onExecute(manager: VKApiManager): Int {
        val call = VKMethodCall.Builder()
            .method("messages.send")
            .args("peer_id", peerId)
            .args("message", text)
            .args("random_id", 0)
            .version(manager.config.version)
            .build()
        return manager.execute(call, ResponseApiParser())
    }

    private class ResponseApiParser : VKApiJSONResponseParser<Int> {
        override fun parse(responseJson: JSONObject): Int {
            try {
                return responseJson.getInt("response")
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }
}