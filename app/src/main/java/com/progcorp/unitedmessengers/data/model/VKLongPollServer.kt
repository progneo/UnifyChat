package com.progcorp.unitedmessengers.data.model

import org.json.JSONObject

data class VKLongPollServer(
    val server: String = "",
    val key: String = "",
    var ts: Long = 0,
    val pts: Long = 0
) {
    companion object {
        fun parse(json: JSONObject) = VKLongPollServer(
            server = json.optString("server"),
            key = json.optString("key"),
            ts = json.optLong("ts"),
            pts = json.optLong("pts")
        )
    }
}