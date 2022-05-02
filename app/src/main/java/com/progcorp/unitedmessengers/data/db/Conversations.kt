package com.progcorp.unitedmessengers.data.db

import android.util.Log
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.db.vk.requests.VKConversationByIdRequest
import com.progcorp.unitedmessengers.data.db.vk.requests.VKConversationsRequest
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONException
import org.json.JSONObject

@ExperimentalCoroutinesApi
class Conversations(private val onChatsFetched: OnConversationsFetched) {
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

    private suspend fun tgGetConversationIds(offset: Long): Flow<LongArray> = callbackFlow {
        App.application.tgClient.client.send(TdApi.GetChats(TdApi.ChatListMain(), 15)) {
            when (it.constructor) {
                TdApi.Chats.CONSTRUCTOR -> this.trySend((it as TdApi.Chats).chatIds).isSuccess
                TdApi.Error.CONSTRUCTOR -> error("")
                else -> error("")
            }
        }
        awaitClose {}
    }

    suspend fun tgGetConversationById(id: Long) = callbackFlow {
        App.application.tgClient.client.send(TdApi.GetChat(id)) {
            when (it.constructor) {
                TdApi.Chat.CONSTRUCTOR -> this.trySend(it as TdApi.Chat).isSuccess
                TdApi.Error.CONSTRUCTOR -> error("")
                else -> error("")
            }
        }
        awaitClose {}
    }

    suspend fun tgGetConversations(offset: Long, isNew: Boolean) {
        tgGetConversationIds(offset)
            .map { ids -> ids.map { tgGetConversationById(it) } }
            .flatMapLatest { chatsFlow ->
                combine(chatsFlow) {
                    chats -> chats.toList()
                    val conversations: ArrayList<Conversation> = arrayListOf()
                    for (chat in chats) {
                        conversations.add(Conversation.tgParse(chat))
                    }
                    onChatsFetched.showConversations(conversations, false)
                }
            }
    }

    interface OnConversationsFetched {
        fun showConversations(chats: ArrayList<Conversation>, isNew: Boolean)
    }

    companion object {
        const val TAG = "Conversations"
    }
}
