package com.progcorp.unitedmessengers.data.db.vk

import android.util.Log
import com.progcorp.unitedmessengers.data.db.vk.requests.VKChatsCommand
import com.progcorp.unitedmessengers.data.model.Conversation
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback

class VKConversations(private val onChatsFetched: OnConversationsFetched) {
    fun getConversations(offset: Int) {
        VK.execute(VKChatsCommand(offset), object: VKApiCallback<List<Conversation>> {
            override fun success(result: List<Conversation>) {
                onChatsFetched.showConversations(result as ArrayList<Conversation>)
            }
            override fun fail(error: Exception) {
                Log.e(TAG, error.toString())
                onChatsFetched.showConversations(arrayListOf<Conversation>())
            }
        })
    }

    interface OnConversationsFetched {
        fun showConversations(chats: ArrayList<Conversation>)
    }

    companion object {
        const val TAG = "VKConversations"
    }
}