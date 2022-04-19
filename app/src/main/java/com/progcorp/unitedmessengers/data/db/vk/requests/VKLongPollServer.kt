package com.progcorp.unitedmessengers.data.db.vk.requests

import com.progcorp.unitedmessengers.data.model.vk.LongPollVK
import com.vk.api.sdk.VKApiJSONResponseParser
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject

class VKLongPollServer(): ApiCommand<LongPollVK>() {
    override fun onExecute(manager: VKApiManager): LongPollVK {
        val call = VKMethodCall.Builder()
            .method("messages.getLongPollServer")
            .args("need_pts", true)
            .version(manager.config.version)
            .build()
        return manager.execute(call, ResponseApiParser())
    }

    private class ResponseApiParser : VKApiJSONResponseParser<LongPollVK> {
        override fun parse(responseJson: JSONObject): LongPollVK {
            try {
                val o = responseJson.getJSONObject("response")
                return LongPollVK.parse(o)
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }
}