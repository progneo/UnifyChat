package com.progcorp.unitedmessengers.data.db

import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.data.model.User
import kotlinx.coroutines.flow.*

class TelegramRepository (private val dataSource: TelegramDataSource) {

    suspend fun getConversations(): Flow<List<Conversation>> =
        dataSource.getConversations(1000)
            .map { conversations -> conversations.mapNotNull { Conversation.tgParse(it) } }

    suspend fun getConversation(chatId: Long): Flow<Conversation> =
        dataSource.getConversation(chatId)
            .mapNotNull { Conversation.tgParse(it) }

    suspend fun getMessages(chatId: Long, fromMessageId: Long, limit: Int): Flow<List<Message>> =
        dataSource.getMessages(chatId, fromMessageId, limit)
            .map { messages -> messages.map { Message.tgParse(it) } }

    suspend fun getUser(userId: Long): Flow<User> =
        dataSource.getUser(userId)
            .map { User.tgParse(it) }

    suspend fun sendMessage(chatId: Long, message: Message): Flow<Long> =
        dataSource.sendMessage(chatId, message)
            .map { message.id }
}
