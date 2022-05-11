package com.progcorp.unitedmessengers.data.db.telegram

import android.util.Log
import com.progcorp.unitedmessengers.App
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.drinkless.td.libcore.telegram.TdApi

@ExperimentalCoroutinesApi
class TgMessagesRepository {

    fun getMessages(chatId: Long, fromMessageId: Long, limit: Int): Flow<List<TdApi.Message>> =
        callbackFlow {
            App.application.tgClient.client.send(TdApi.GetChatHistory(chatId, fromMessageId, 0, limit, false)) {
                when (it.constructor) {
                    TdApi.Messages.CONSTRUCTOR -> {
                        trySend((it as TdApi.Messages).messages.toList()).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e("getMessages", "${(it as TdApi.Error).message}. ID: $chatId")
                    }
                    else -> {
                        Log.e("getMessages", "Something went wrong")
                    }
                }
            }
            awaitClose { }
        }

    fun getMessage(chatId: Long, messageId: Long): Flow<TdApi.Message> =
        callbackFlow {
            App.application.tgClient.client.send(TdApi.GetMessage(chatId, messageId)) {
                when (it.constructor) {
                    TdApi.Message.CONSTRUCTOR -> {
                        trySend(it as TdApi.Message).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e("getMessage", "${(it as TdApi.Error).message}. ID: $chatId")
                    }
                    else -> {
                        Log.e("getMessage", "Something went wrong")
                    }
                }
            }
            awaitClose { }
        }

    fun sendMessage(chatId: Long, message: String): Flow<TdApi.Message> =
        callbackFlow {
            val text = TdApi.FormattedText(message, arrayOf(TdApi.TextEntity()))
            val input = TdApi.InputMessageText(text, true, false)
            App.application.tgClient.client.send(TdApi.SendMessage(
                chatId,
                0,
                0,
                null,
                null,
                input)) {
                when (it.constructor) {
                    TdApi.Message.CONSTRUCTOR -> {
                        trySend(it as TdApi.Message).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e("sendMessage", "${(it as TdApi.Error).message}. ID: $chatId")
                    }
                    else -> {
                        Log.e("sendMessage", "Something went wrong")
                    }
                }
            }
            awaitClose { }
        }
}