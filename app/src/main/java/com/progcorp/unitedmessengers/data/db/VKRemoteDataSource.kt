package com.progcorp.unitedmessengers.data.db

import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.ApiResult
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.interfaces.requests.*
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class VKRemoteDataSource @Inject constructor(private val retrofit: Retrofit) {

    suspend fun getConversations(offset: Int): ApiResult<String> {
        val service = retrofit.create(VKConversationsRequest::class.java)
        return getResponse(
            request = {
                service.conversationsGet(
                    App.application.vkAccountService.token!!,
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
                    App.application.vkAccountService.token!!,
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
                    App.application.vkAccountService.token!!,
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
                    App.application.vkAccountService.token!!,
                    "5.131",
                    "photo_100",
                    0
                )
            }
        )
    }

    suspend fun sendMessage(message: Message): ApiResult<String> {
        val service = retrofit.create(VKSendMessageRequest::class.java)
        return getResponse(
            request = {
                service.messageSend(
                    App.application.vkAccountService.token!!,
                    "5.131",
                    message.peerId,
                    message.text,
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