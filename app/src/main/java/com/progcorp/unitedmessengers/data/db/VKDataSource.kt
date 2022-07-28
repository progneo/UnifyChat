package com.progcorp.unitedmessengers.data.db

import android.util.Log
import com.progcorp.unitedmessengers.data.Resource
import com.progcorp.unitedmessengers.data.clients.VKClient
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.data.model.MessageText
import com.progcorp.unitedmessengers.interfaces.requests.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class VKDataSource (private val client: VKClient) {
    private val _retrofit = Retrofit.Builder()
        .baseUrl("https://api.vk.com/method/")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    suspend fun getConversations(offset: Int): Resource<String> {
        val service = _retrofit.create(VKConversationsRequest::class.java)
        return getResponse(
            request = {
                service.conversationsGet(
                    client.token!!,
                    "5.131",
                    15,
                    offset,
                    true,
                    0
                )
            }
        )
    }

    suspend fun getUnreadCount(): Resource<String> {
        val service = _retrofit.create(VKConversationsRequest::class.java)
        return getResponse(
            request = {
                service.conversationsGet(
                    client.token!!,
                    "5.131",
                    0,
                    0,
                    false,
                    0
                )
            }
        )
    }

    suspend fun getConversationById(id: Long): Resource<String> {
        val service = _retrofit.create(VKConversationByIdRequest::class.java)
        return getResponse(
            request = {
                service.conversationGet(
                    client.token!!,
                    "5.131",
                    id,
                    true,
                    0
                )
            }
        )
    }

    suspend fun getMessagesFromId(conversationId: Long, fromId: Long, count: Int): Resource<String> {
        val service = _retrofit.create(VKMessagesRequest::class.java)
        return getResponse(
            request = {
                service.messagesGetFromId(
                    client.token!!,
                    "5.131",
                    count,
                    fromId,
                    conversationId,
                    true,
                    0
                )
            }
        )
    }

    suspend fun getMessages(conversationId: Long, count: Int): Resource<String> {
        val service = _retrofit.create(VKMessagesRequest::class.java)
        return getResponse(
            request = {
                service.messagesGet(
                    client.token!!,
                    "5.131",
                    count,
                    conversationId,
                    true,
                    0
                )
            }
        )
    }

    suspend fun getUser(): Resource<String> {
        val service = _retrofit.create(VKUsersRequest::class.java)
        return getResponse(
            request = {
                service.userGet(
                    client.token!!,
                    "5.131",
                    "photo_100",
                    0
                )
            }
        )
    }

    suspend fun getUsers(userIds: String): Resource<String> {
        val service = _retrofit.create(VKUsersRequest::class.java)
        return getResponse(
            request = {
                service.usersGet(
                    client.token!!,
                    "5.131",
                    userIds,
                    "photo_100",
                    0
                )
            }
        )
    }

    suspend fun sendMessage(chatId: Long, message: Message): Resource<String> {
        val service = _retrofit.create(VKSendMessageRequest::class.java)
        return getResponse(
            request = {
                service.messageSend(
                    client.token!!,
                    "5.131",
                    chatId,
                    (message.content as MessageText).text,
                    0,
                    message.replyToMessage?.id ?: 0,
                    0
                )
            }
        )
    }

    suspend fun markAsRead(chatId: Long, message: Message): Resource<String> {
        val service = _retrofit.create(VKMarkAsRead::class.java)
        return getResponse(
            request = {
                service.markAsRead(
                    client.token!!,
                    "5.131",
                    chatId,
                    message.id,
                    0
                )
            }
        )
    }

    suspend fun deleteMessages(messageIds: String, deleteForAll: Boolean): Resource<String> {
        val service = _retrofit.create(VKDeleteMessages::class.java)
        val response: Resource<String>
        if (deleteForAll) {
            response = getResponse(
                request = {
                    service.deleteMessagesForAll(
                        client.token!!,
                        "5.131",
                        messageIds,
                        deleteForAll
                    )
                }
            )
        }
        else {
            response = getResponse(
                request = {
                    service.deleteMessages(
                        client.token!!,
                        "5.131",
                        messageIds
                    )
                }
            )
        }
        return response
    }

    suspend fun getMessagesById(messageIds: String): Resource<String> {
        val service = _retrofit.create(VKMessagesRequest::class.java)
        return getResponse(
            request = {
                service.messagesGetById(
                    client.token!!,
                    "5.131",
                    messageIds,
                    true,
                    0
                )
            }
        )
    }

    suspend fun editMessage(conversation: Conversation, message: Message): Resource<String> {
        val service = _retrofit.create(VKEditMessage::class.java)
        return getResponse(
            request = {
                service.editMessage(
                    client.token!!,
                    "5.131",
                    conversation.id,
                    message.content.text,
                    keep_forward_messages = true,
                    keep_snippets = true,
                    disable_mentions = true,
                    message_id = message.id.toString()
                )
            }
        )
    }

    suspend fun getLongPollServer(): Resource<String> {
        val service = _retrofit.create(VKGetLongPollServer::class.java)
        return getResponse(
            request = {
                service.messagesLongPollServerGet(
                    client.token!!,
                    "5.131",
                    true,
                    "3"
                )
            }
        )
    }

    suspend fun getLongPollHistory(): Resource<String> {
        return if (client.lpRetrofit != null) {
            val service = client.lpRetrofit!!.create(VKGetLongPollHistory::class.java)
            getResponse(
                request = {
                    service.messagesLongPollHistoryGet(
                        client.lpServer!!.server,
                        client.lpServer!!.key,
                        client.lpServer!!.ts,
                        client.lpServer!!.pts,
                        25,
                        32,
                        "3",
                        true
                    )
                }
            )
        }
        else Resource.error("Long poll retrofit not loaded", null)
    }

    private suspend fun <T> getResponse(request: suspend () -> Response<T>): Resource<T> {
        return try {
            val result = request.invoke()
            if (result.isSuccessful) {
                return Resource.success(result.body())
            }
            else {
                val error = result.errorBody()?.toString()
                result.errorBody()?.close()
                Resource.error(error!!, null)
            }
        } catch (e: Throwable) {
            Log.e("VKDataSource.getResponse", e.stackTraceToString())
            Resource.error("Unknown Error", null)
        }
    }
}