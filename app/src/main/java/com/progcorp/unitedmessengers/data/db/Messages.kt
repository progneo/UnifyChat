package com.progcorp.unitedmessengers.data.db

import android.util.Log
import com.progcorp.unitedmessengers.data.db.vk.requests.VKMessagesCommand
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback

class Messages(private val onMessagesFetched: OnMessagesFetched) {
    fun vkGetMessages(chat: Conversation, offset: Int, count: Int, isNew: Boolean) {
        VK.execute(VKMessagesCommand(chat.id, offset, count), object : VKApiCallback<List<Message>> {
            override fun success(result: List<Message>) {
                onMessagesFetched.showMessages(result as ArrayList<Message>, isNew)
            }

            override fun fail(error: Exception) {
                Log.e(TAG, error.toString())
                onMessagesFetched.showMessages(arrayListOf<Message>(), isNew)
            }
        })
    }

    interface OnMessagesFetched {
        fun showMessages(messages: ArrayList<Message>, isNew: Boolean)
    }

    companion object {
        const val TAG = "Messages"
    }
}