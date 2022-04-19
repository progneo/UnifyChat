package com.progcorp.unitedmessengers.data.db.vk.requests

import com.vk.api.sdk.VKApiJSONResponseParser
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import com.progcorp.unitedmessengers.data.model.User
import org.json.JSONException
import org.json.JSONObject

class VKUsersCommand(private val uids: IntArray = intArrayOf()): ApiCommand<List<User>>() {
    override fun onExecute(manager: VKApiManager): List<User> {

        if (uids.isEmpty()) {
            val call = VKMethodCall.Builder()
                .method("users.get")
                .args("fields", "photo_100")
                .version(manager.config.version)
                .build()
            return manager.execute(call, ResponseApiParser())
        } else {
            val result = ArrayList<User>()
            val chunks = uids.toList().chunked(CHUNK_LIMIT)
            for (chunk in chunks) {
                val call = VKMethodCall.Builder()
                    .method("users.get")
                    .args("user_ids", chunk.joinToString(","))
                    .args("fields", "photo_100, online, last_seen")
                    .args("lang", 0)
                    .version(manager.config.version)
                    .build()
                result.addAll(manager.execute(call, ResponseApiParser()))
            }
            return result
        }
    }

    companion object {
        const val CHUNK_LIMIT = 900
    }

    private class ResponseApiParser : VKApiJSONResponseParser<List<User>> {
        override fun parse(responseJson: JSONObject): List<User> {
            try {
                val ja = responseJson.getJSONArray("response")
                val r = ArrayList<User>(ja.length())
                for (i in 0 until ja.length()) {
                    val user = User.parse(ja.getJSONObject(i))
                    r.add(user)
                }
                return r
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }
}