package com.progcorp.unitedmessengers.data.model

import android.util.Log
import com.progcorp.unitedmessengers.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject

data class VKLongPollServer(
    val server: String = "",
    val key: String = "",
    var ts: Long = 0,
    var pts: Long = 0
) {

    fun parseResponse(json: JSONObject) {
        ts = json.optLong("ts")
        pts = json.optLong("pts")
        MainScope().launch(Dispatchers.Main) {
            json.optJSONArray("updates")?.let { updates ->
                val client = App.application.vkClient
                val newMessageIds = arrayListOf<Long>()
                val updatedMessageIds = arrayListOf<Long>()
                for (i in 0 until updates.length()) {
                    updates.getJSONArray(i).let { update ->
                        //https://dev.vk.com/api/user-long-poll/getting-started#61
                        Log.d("longPoll", update.toString())
                        when (update.getInt(0)) {
                            2 -> {
                                if (update.getLong(2) - 131072 >= 0) {
                                    client.changeUpdateState(
                                        VKUpdateDeleteMessage(update.getLong(1), update.getLong(3))
                                    )
                                } else if (update.getLong(2) == 128.toLong()) {
                                    client.changeUpdateState(
                                        VKUpdateDeleteMessage(update.getLong(1), update.getLong(3))
                                    )
                                }
                                else { println() }
                            }
                            4 -> {
                                newMessageIds.add(update.getLong(1))
                            }
                            5 -> {
                                updatedMessageIds.add(update.getLong(1))
                            }
                            8 -> {
                                client.changeUpdateState(
                                    VKUpdateUserStatus(
                                        update.getLong(1) * -1,
                                        true,
                                        0)
                                )
                            }
                            9 -> {
                                client.changeUpdateState(
                                    VKUpdateUserStatus(
                                        update.getLong(1) * -1,
                                        false,
                                        update.getLong(3))
                                )
                            }
                            61 -> {

                            }
                            62 -> {

                            }
                            63 -> {

                            }
                            64 -> {

                            }
                            80 -> {
                                client.changeUpdateState(
                                    VKUpdateUnreadCount(update.getInt(1))
                                )
                            }
                            else -> {}
                        }
                    }
                }
                if (newMessageIds.size > 0) {
                    val data = client.repository.getMessagesById(newMessageIds).first()
                    client.changeUpdateState(
                        VKUpdateNewMessages(data)
                    )
                }
                if (updatedMessageIds.size > 0) {
                    val data = client.repository.getMessagesById(updatedMessageIds).first()
                    client.changeUpdateState(
                        VKUpdateMessagesContent(data)
                    )
                }
            }
        }
    }
    companion object {
        private fun fixServer(server: String): String {
            return server.removePrefix("im.vk.com/")
        }

        fun parseFull(json: JSONObject) = VKLongPollServer(
            server = fixServer(json.optString("server")),
            key = json.optString("key"),
            ts = json.optLong("ts"),
            pts = json.optLong("pts")
        )
    }
}