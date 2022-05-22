package com.progcorp.unitedmessengers.data.db

import android.util.Log
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.data.model.User
import com.progcorp.unitedmessengers.enums.ApiStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONException
import org.json.JSONObject

class VKRepository (private val dataSource: VKDataSource) {

    suspend fun getConversations(offset: Int): Flow<List<Conversation>> {
        return flow {
            val response = dataSource.getConversations(offset)
            if (response.status == ApiStatus.SUCCESS) {
                val result: ArrayList<Conversation> = arrayListOf()
                val json = response.data?.let { JSONObject(it) }
                if (json != null) {
                    try {
                        val items = json.getJSONObject("response").getJSONArray("items")
                        for (item in 0 until items.length()) {
                            val chat = Conversation.vkParse(
                                items.getJSONObject(item),
                                json.getJSONObject("response").optJSONArray("profiles"),
                                json.getJSONObject("response").optJSONArray("groups")
                            )
                            result.add(chat)
                        }
                    } catch (ex: JSONException) {
                        Log.e("${javaClass.simpleName}.getConversations", ex.stackTraceToString())
                    }
                }
                emit(result as List<Conversation>)
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getConversation(id: Int): Flow<Conversation?>  {
        return flow {
            val response = dataSource.getConversationById(id)
            var result: Conversation? = null
            if (response.status == ApiStatus.SUCCESS) {
                try {
                    val json = response.data?.let { JSONObject(it) }
                    if (json != null) {
                        val items = json.getJSONObject("response").getJSONArray("items")
                        result = Conversation.vkParse(
                            items.getJSONObject(0),
                            json.getJSONObject("response").optJSONArray("profiles"),
                            json.getJSONObject("response").optJSONArray("groups")
                        )
                    }
                }
                catch (ex: JSONException) {
                    Log.e("${javaClass.simpleName}.getConversationById", ex.stackTraceToString())
                }
            }
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getMessages(chat: Conversation, offset: Int, count: Int): Flow<List<Message>> {
        return flow {
            val response = dataSource.getMessages(chat, offset, count)
            if (response.status == ApiStatus.SUCCESS) {
                val result: ArrayList<Message> = arrayListOf()
                val json = response.data?.let { JSONObject(it) }
                if (json != null) {
                    try {
                        val messages = json.getJSONObject("response").getJSONArray("items")
                        val profiles = json.getJSONObject("response").optJSONArray("profiles")
                        for (item in 0 until messages.length()) {
                            result.add(Message.vkParse(messages.getJSONObject(item), profiles))
                        }
                    } catch (ex: JSONException) {
                        Log.e("${javaClass.simpleName}.getMessages", ex.stackTraceToString())
                    }
                }
                emit(result as List<Message>)
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getUsers(): Flow<List<User>> {
        return flow {
            val response = dataSource.getUsers()
            if (response.status == ApiStatus.SUCCESS) {
                val result: ArrayList<User> = arrayListOf()
                val json = response.data?.let { JSONObject(it) }
                if (json != null) {
                    try {
                        val users = json.getJSONArray("response")
                        for (i in 0 until users.length()) {
                            result.add(User.vkParse(users.getJSONObject(i)))
                        }
                    } catch (ex: JSONException) {
                        Log.e("${javaClass.simpleName}.getUsers", ex.stackTraceToString())
                    }
                }
                emit(result as List<User>)
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendMessage(message: Message): Flow<Long> {
        return flow {
            val response = dataSource.sendMessage(message)
            if (response.status == ApiStatus.SUCCESS) {
                val result: Long = 0
                val json = response.data?.let { JSONObject(it) }
                if (json != null) {
                    try {
                        json.getLong("response")
                    } catch (ex: JSONException) {
                        Log.e("${javaClass.simpleName}.sendMessage", ex.stackTraceToString())
                    }
                }
                emit(result)
            }
        }.flowOn(Dispatchers.IO)
    }
}