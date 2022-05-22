package com.progcorp.unitedmessengers.data.db

import com.progcorp.unitedmessengers.data.ApiResult
import com.progcorp.unitedmessengers.data.clients.VKClient
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.data.model.MessageText
import com.progcorp.unitedmessengers.interfaces.requests.*
import retrofit2.Response
import retrofit2.Retrofit

class VKDataSource (
    private val retrofit: Retrofit,
    private val accountService: VKClient
) {

    suspend fun getConversations(offset: Int): ApiResult<String> {
        val service = retrofit.create(VKConversationsRequest::class.java)
        return getResponse(
            request = {
                service.conversationsGet(
                    accountService.token!!,
                    "5.131",
                    15,
                    offset,
                    true,
                    0
                )
            }
        )
    }

    suspend fun getConversationById(id: Int): ApiResult<String> {
        val service = retrofit.create(VKConversationByIdRequest::class.java)
        return getResponse(
            request = {
                service.conversationGet(
                    accountService.token!!,
                    "5.131",
                    id,
                    true,
                    0
                )
            }
        )
    }

    suspend fun getMessages(chat: Conversation, offset: Int, count: Int): ApiResult<String> {
        val service = retrofit.create(VKMessagesRequest::class.java)
        return getResponse(
            request = {
                service.messagesGet(
                    accountService.token!!,
                    "5.131",
                    count,
                    offset,
                    chat.id,
                    true,
                    0
                )
            }
        )
    }

    suspend fun getUsers(): ApiResult<String> {
        val service = retrofit.create(VKUsersRequest::class.java)
        return getResponse(
            request = {
                service.usersGet(
                    accountService.token!!,
                    "5.131",
                    "photo_100",
                    0
                )
            }
        )
    }

    suspend fun sendMessage(chatId: Long, message: Message): ApiResult<String> {
        val service = retrofit.create(VKSendMessageRequest::class.java)
        return getResponse(
            request = {
                service.messageSend(
                    accountService.token!!,
                    "5.131",
                    chatId,
                    (message.content as MessageText).text,
                    0,
                    0
                )
            }
        )
    }

    private suspend fun <T> getResponse(request: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val result = request.invoke()
            if (result.isSuccessful) {
                return ApiResult.Success(result.body())
            }
            else {
                val error = result.errorBody()?.toString()
                result.errorBody()?.close()
                ApiResult.Error(error!!)
            }
        } catch (e: Throwable) {
            ApiResult.Error("Unknown Error")
        }
    }
}