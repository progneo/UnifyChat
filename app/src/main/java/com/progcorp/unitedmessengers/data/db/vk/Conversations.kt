package com.progcorp.unitedmessengers.data.db.vk

import android.util.Log
import com.progcorp.unitedmessengers.data.db.vk.requests.VKChatsCommand
import com.progcorp.unitedmessengers.data.model.Conversation
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback

class Conversations(private val onChatsFetched: OnConversationsFetched) {
    fun vkGetConversations(offset: Int, isNew: Boolean) {
        VK.execute(VKChatsCommand(offset), object: VKApiCallback<List<Conversation>> {
            override fun success(result: List<Conversation>) {
                onChatsFetched.showConversations(result as ArrayList<Conversation>, isNew)
            }
            override fun fail(error: Exception) {
                Log.e(TAG, error.toString())
                onChatsFetched.showConversations(arrayListOf<Conversation>(), isNew)
            }
        })
    }

    interface OnConversationsFetched {
        fun showConversations(chats: ArrayList<Conversation>, isNew: Boolean)
    }

    companion object {
        const val TAG = "VKConversations"
    }
}