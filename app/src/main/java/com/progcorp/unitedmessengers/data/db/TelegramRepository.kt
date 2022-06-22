@file:OptIn(ExperimentalCoroutinesApi::class)

package com.progcorp.unitedmessengers.data.db

import com.progcorp.unitedmessengers.data.Resource
import com.progcorp.unitedmessengers.data.model.companions.Chat
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.data.model.companions.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.TdApi

class TelegramRepository (private val dataSource: TelegramDataSource) {

    suspend fun getConversations(): Flow<Resource<List<Conversation>>> = callbackFlow {
        val list = arrayListOf<Conversation>()
        val data = dataSource.getConversations(1).first()
        for (conversation in data) {
            Conversation.tgParse(conversation)?.let { list.add(it) }
        }
        trySend(Resource.success(list)).isSuccess
        awaitClose()
    }

    suspend fun getConversation(chatId: Long): Flow<Resource<Conversation>> {
        return flow {
            val data = dataSource.getConversation(chatId).first()
            val chat = Conversation.tgParse(data)
            if (chat == null) {
                emit(Resource.error("Chat is null", null))
            }
            else {
                emit(Resource.success(chat))
            }
        }
    }

    suspend fun getSupergroup(groupId: Long, conversation: TdApi.Chat): Flow<Resource<Chat>> = callbackFlow  {
        val data = dataSource.getSupergroup(groupId).first()
        trySend(Resource.success(Chat.tgParseSupergroup(conversation, data))).isSuccess
        awaitClose()
    }

    suspend fun getBasicGroup(chatId: Long, conversation: TdApi.Chat): Flow<Resource<Chat>> = callbackFlow {
        val data = dataSource.getBasicGroup(chatId).first()
        trySend(Resource.success(Chat.tgParseBasicGroup(conversation, data))).isSuccess
        awaitClose()
    }

    suspend fun getMessages(chatId: Long, fromMessageId: Long, limit: Int): Flow<Resource<List<Message>>> = callbackFlow {
        val list = arrayListOf<Message>()
        val data = dataSource.getMessages(chatId, fromMessageId, limit).first()
        for (message in data) {
            list.add(Message.tgParse(message))
        }
        trySend(Resource.success(list)).isSuccess
        awaitClose()
    }

    fun getUser(userId: Long): Flow<Resource<User>> = callbackFlow {
        val data = dataSource.getUser(userId).first()
        trySend(Resource.success(User.tgParse(data))).isSuccess
        awaitClose()
    }

    suspend fun sendMessage(chatId: Long, message: Message): Flow<Resource<Long>> = callbackFlow {
        val data = dataSource.sendMessage(chatId, message).first()
        trySend(Resource.success(data.id)).isSuccess
        awaitClose()
    }
}
