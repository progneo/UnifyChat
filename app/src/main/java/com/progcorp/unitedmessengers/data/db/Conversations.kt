package com.progcorp.unitedmessengers.data.db

import android.util.Log
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.db.telegram.TgConversationsRepository
import com.progcorp.unitedmessengers.interfaces.requests.VKConversationByIdRequest
import com.progcorp.unitedmessengers.interfaces.requests.VKConversationsRequest
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception

class Conversations {
    suspend fun tgGetConversations(): ArrayList<Conversation> {
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
            }
        }
        catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
        }
    }

    companion object {
        const val TAG = "Conversations"
    }
}
