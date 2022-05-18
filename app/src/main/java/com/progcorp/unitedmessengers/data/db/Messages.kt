package com.progcorp.unitedmessengers.data.db

import android.util.Log
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.db.telegram.TgConversationsRepository
import com.progcorp.unitedmessengers.data.db.telegram.TgMessagesRepository
import com.progcorp.unitedmessengers.data.db.telegram.TgUserRepository
import com.progcorp.unitedmessengers.interfaces.requests.VKMessagesRequest
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception

class Messages(private val onMessagesFetched: OnMessagesFetched) {

    suspend fun tgGetMessages(chatId: Long, fromMessageId: Long, limit: Int, isNew: Boolean) {
        try {
            MainScope().launch {
                val chat = TgConversationsRepository().getChat(chatId).first()
                val response = TgMessagesRepository().getMessages(chatId, fromMessageId, limit)
                val tgMessages = response.first()
                val messages: ArrayList<Message> = arrayListOf()
                for (message in tgMessages) {
                    val messageObj = Message.tgParse(message, chat)
                    messages.add(messageObj)
                    launch {
                        when (message.senderId::class.simpleName) {
                            "MessageSenderUser" -> {
                                val messageSender = message.senderId as TdApi.MessageSenderUser
                                val sender = TgUserRepository().getUser(messageSender.userId).first()
                                if (sender.profilePhoto != null) {
                                    launch {
                                        val photo = App.application.tgClient.downloadableFile(sender.profilePhoto!!.small).first()
                                        if (photo != null) {
                                            messageObj.senderPhoto = photo
                                        }
                                    }
                                }
                            }
                            else -> {
                                val messageSender = message.senderId as TdApi.MessageSenderChat
                                val sender = TgConversationsRepository().getChat(messageSender.chatId).first()
                                if (sender.photo != null) {
                                    launch {
                                        val photo = App.application.tgClient.downloadableFile(sender.photo!!.small).first()
                                        if (photo != null) {
                                            messageObj.senderPhoto = photo
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                onMessagesFetched.showMessages(messages, isNew)
            }
        }
        catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
        }
    }

    interface OnMessagesFetched {
        fun showMessages(messages: ArrayList<Message>, isNew: Boolean)
    }

    companion object {
        const val TAG = "Messages"
    }
}