package com.progcorp.unitedmessengers.data.db.vk.requests

import com.progcorp.unitedmessengers.data.model.Conversation
import com.vk.api.sdk.VKApiJSONResponseParser
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject

class VKConversationByIdCommand(private val id: Int): ApiCommand<List<Conversation>>(){
    override fun onExecute(manager: VKApiManager): List<Conversation>  {
        val call = VKMethodCall.Builder()
            .method("messages.getConversationsById")
            .args("peer_ids", id)
            .args("extended", true)
            .args("lang", 0)
            .version(manager.config.version)
            .build()
        return manager.execute(call, ResponseApiParser())
    }

    private class ResponseApiParser : VKApiJSONResponseParser<List<Conversation> > {
        override fun parse(responseJson: JSONObject): List<Conversation>  {
            try {
                val o = responseJson.getJSONObject("response").getJSONArray("items")
                return listOf(Conversation.parse(
                    o.getJSONObject(0),
                    responseJson.getJSONObject("response").optJSONArray("profiles"),
                    responseJson.getJSONObject("response").optJSONArray("groups")
                ))
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }
}
