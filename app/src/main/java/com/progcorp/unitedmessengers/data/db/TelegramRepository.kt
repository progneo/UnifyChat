package com.progcorp.unitedmessengers.data.db

import com.progcorp.unitedmessengers.data.model.Chat
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.data.model.User
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.TdApi

class TelegramRepository (private val dataSource: TelegramDataSource) {

    suspend fun getConversations(): Flow<List<Conversation>> =
        dataSource.getConversations(1000)
            .map { conversations -> conversations.mapNotNull { Conversation.tgParse(it) } }

    suspend fun getConversation(chatId: Long): Flow<Conversation> =
        dataSource.getConversation(chatId)
            .mapNotNull { Conversation.tgParse(it) }

    suspend fun getSupergroup(groupId: Long, conversation: TdApi.Chat): Flow<Chat> =
        dataSource.getSupergroup(groupId)
            .mapNotNull { Chat.tgParseSupergroup(conversation, it) }

    suspend fun getBasicGroup(chatId: Long, conversation: TdApi.Chat): Flow<Chat> =
        dataSource.getBasicGroup(chatId)
            .mapNotNull { Chat.tgParseBasicGroup(conversation, it) }

    suspend fun getMessages(chatId: Long, fromMessageId: Long, limit: Int): Flow<List<Message>> =
        dataSource.getMessages(chatId, fromMessageId, limit)
            .map { messages -> messages.map { Message.tgParse(it) } }

    fun getUser(userId: Long): Flow<User> =
        dataSource.getUser(userId)
            .map { User.tgParse(it) }

    suspend fun sendMessage(chatId: Long, message: Message): Flow<Long> =
        dataSource.sendMessage(chatId, message)
            .map { message.id }
}
