package com.progcorp.unitedmessengers.data.db.telegram

import android.util.Log
import com.progcorp.unitedmessengers.App
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.TdApi

@ExperimentalCoroutinesApi
class TgConversationsRepository {

    private fun getChatIds(limit: Int): Flow<LongArray> =
        callbackFlow {
            App.application.tgClient.client.send(TdApi.GetChats(TdApi.ChatListMain(), limit)) {
                when (it.constructor) {
                    TdApi.Chats.CONSTRUCTOR -> {
                        trySend((it as TdApi.Chats).chatIds).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e("getChats", (it as TdApi.Error).message)
                    }
                    else -> {
                        Log.e("getBasicGroup", "Something went wrong")
                    }
                }
            }
            awaitClose { }
        }

    fun getChats(limit: Int): Flow<List<TdApi.Chat>> =
        getChatIds(limit)
            .map { ids -> ids.map { getChat(it) } }
            .flatMapLatest { chatsFlow ->
                combine(chatsFlow) { chats ->
                    chats.toList()
                }
            }

    private fun getChat(chatId: Long): Flow<TdApi.Chat> = callbackFlow {
        App.application.tgClient.client.send(TdApi.GetChat(chatId)) {
            when (it.constructor) {
                TdApi.Chat.CONSTRUCTOR -> {
                    trySend(it as TdApi.Chat).isSuccess
                }
                TdApi.Error.CONSTRUCTOR -> {
                    Log.e("getChat", "${(it as TdApi.Error).message}. ID: $chatId")
                }
                else -> {
                    Log.e("getBasicGroup", "Something went wrong")
                }
            }
        }
        awaitClose { }
    }

    fun chatImage(chat: TdApi.Chat): Flow<String?> =
        chat.photo?.small?.takeIf {
            it.local?.isDownloadingCompleted == false
        }?.id?.let { fileId ->
            App.application.tgClient.downloadFile(fileId).map { chat.photo?.small?.local?.path }
        } ?: flowOf(chat.photo?.small?.local?.path)


    fun getSupergroup(chatId: Long): Flow<TdApi.Supergroup> =
        callbackFlow {
            App.application.tgClient.client.send(TdApi.GetSupergroup(chatId)) {
                when (it.constructor) {
                    TdApi.Supergroup.CONSTRUCTOR -> {
                        trySend(it as TdApi.Supergroup).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e("getSupergroup", "${(it as TdApi.Error).message}. ID: $chatId")
                    }
                    else -> {
                        Log.e("getBasicGroup", "Something went wrong")
                    }
                }
            }
            awaitClose { }
        }

    fun getBasicGroup(chatId: Long): Flow<TdApi.BasicGroup> =
        callbackFlow {
            App.application.tgClient.client.send(TdApi.GetBasicGroup(chatId)) {
                when (it.constructor) {
                    TdApi.BasicGroup.CONSTRUCTOR -> {
                        trySend(it as TdApi.BasicGroup).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e("getBasicGroup", "${(it as TdApi.Error).message}. ID: $chatId")
                    }
                    else -> {
                        Log.e("getBasicGroup", "Something went wrong")
                    }
                }
            }
            awaitClose { }
        }
}