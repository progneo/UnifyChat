package com.progcorp.unitedmessengers.data.db

import android.util.Log
import com.progcorp.unitedmessengers.data.Resource
import com.progcorp.unitedmessengers.data.model.Chat
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.TdApi

class TelegramRepository (private val dataSource: TelegramDataSource) {

    suspend fun getConversations(): Flow<Resource<List<Conversation>>> {
        return flow {
            val list = arrayListOf<Conversation>()
            //emit(Resource.loading(null))
            val data = dataSource.getConversations(100).first()
            Log.i("conversations count", data.size.toString())
            for (conversation in data) {
                Log.i("conversations count", conversation.title.toString())
                Conversation.tgParse(conversation)?.let { list.add(it) }
            }
            emit(Resource.success(list))
        }
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

    suspend fun getSupergroup(groupId: Long, conversation: TdApi.Chat): Flow<Resource<Chat>> {
        return flow {
            val data = dataSource.getSupergroup(groupId).first()
            emit(Resource.success(Chat.tgParseSupergroup(conversation, data)))
        }
    }

    suspend fun getBasicGroup(chatId: Long, conversation: TdApi.Chat): Flow<Resource<Chat>> {
        return flow {
            val data = dataSource.getBasicGroup(chatId).first()
            emit(Resource.success(Chat.tgParseBasicGroup(conversation, data)))
        }
    }

    suspend fun getMessages(chatId: Long, fromMessageId: Long, limit: Int): Flow<Resource<List<Message>>> {
        return flow {
            val list = arrayListOf<Message>()
            val data = dataSource.getMessages(chatId, fromMessageId, limit).first()
            for (message in data) {
                list.add(Message.tgParse(message))
            }
            emit(Resource.success(list))
        }
    }

    fun getUser(userId: Long): Flow<Resource<User>> {
        return flow {
            val data = dataSource.getUser(userId).first()
            emit(Resource.success(User.tgParse(data)))
        }
    }

    suspend fun sendMessage(chatId: Long, message: Message): Flow<Resource<Long>> {
        return flow {
            val data = dataSource.sendMessage(chatId, message).first()
            emit(Resource.success(data.id))
        }
    }
}
