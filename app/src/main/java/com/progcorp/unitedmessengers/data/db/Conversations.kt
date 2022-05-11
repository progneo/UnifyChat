package com.progcorp.unitedmessengers.data.db

import android.util.Log
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.db.telegram.TgConversationsRepository
import com.progcorp.unitedmessengers.data.db.vk.requests.VKConversationByIdRequest
import com.progcorp.unitedmessengers.data.db.vk.requests.VKConversationsRequest
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception

class Conversations(
        private val onChatsFetched: OnConversationsFetched
    ) {
    suspend fun vkGetConversations(offset: Int, isNew: Boolean) {
        val response = App.application.vkRetrofit.create(VKConversationsRequest::class.java)
            .conversationsGet(
                App.application.vkAccountService.token!!, "5.131", 15, offset, true, 0
            )
        val responseJson = JSONObject(response)
        try {
            val o = responseJson.getJSONObject("response").getJSONArray("items")
            val r = ArrayList<Conversation>(o.length())
            for (i in 0 until o.length()) {
                val chat = Conversation.vkParse(
                    o.getJSONObject(i),
                    responseJson.getJSONObject("response").optJSONArray("profiles"),
                    responseJson.getJSONObject("response").optJSONArray("groups")
                )
                r.add(chat)
            }
            onChatsFetched.showConversations(r, isNew)
        } catch (ex: JSONException) {
            Log.e(ConversationViewModel.TAG, ex.stackTraceToString())
        }
    }

    suspend fun vkGetConversationById(id: Int) {
        val response = App.application.vkRetrofit.create(VKConversationByIdRequest::class.java)
            .conversationGet(
                App.application.vkAccountService.token!!, "5.131", id, true, 0
            )
        val responseJson = JSONObject(response)
        try {
            val o = responseJson.getJSONObject("response").getJSONArray("items")
            val result = listOf(
                Conversation.vkParse(
                    o.getJSONObject(0),
                    responseJson.getJSONObject("response").optJSONArray("profiles"),
                    responseJson.getJSONObject("response").optJSONArray("groups")
                )
            )
            onChatsFetched.showConversations(result as ArrayList<Conversation>, false)
        } catch (ex: JSONException) {
            Log.e(ConversationViewModel.TAG, ex.stackTraceToString())
        }
    }

    suspend fun tgGetConversations() {
        try {
            MainScope().launch {
                val response = TgConversationsRepository().getChats(1000)
                val chats = response.first()
                val conversations: ArrayList<Conversation> = arrayListOf()
                for (chat in chats) {
                    if (chat.positions.isNotEmpty()) {
                        val conversation = Conversation.tgParse(chat)
                        if (conversation != null) {
                            conversations.add(conversation)
                        }
                    }
                }
                onChatsFetched.showConversations(conversations, false)
            }
        }
        catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
        }
    }

    interface OnConversationsFetched {
        fun showConversations(chats: ArrayList<Conversation>, isNew: Boolean)
    }

    companion object {
        const val TAG = "Conversations"
    }
}
