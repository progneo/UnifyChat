package com.progcorp.unitedmessengers.data.db.telegram

import android.util.Log
import com.progcorp.unitedmessengers.App
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.drinkless.td.libcore.telegram.TdApi

@ExperimentalCoroutinesApi
class TgSupergroupFullInfoRepository {
    fun getInfo(chatId: Long): Flow<TdApi.Object> =
        callbackFlow {
            App.application.tgClient.client.send(TdApi.GetSupergroupFullInfo(chatId)) {
                when (it.constructor) {
                    TdApi.ChatMembers.CONSTRUCTOR -> {
                        trySend(it as TdApi.SupergroupFullInfo).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        trySend(it as TdApi.Error).isSuccess
                        Log.e("TgMembersRepository", "${(it as TdApi.Error).message}. ID: $chatId")
                    }
                    else -> {
                        error("")
                    }
                }
                //close()
            }
            awaitClose { }
        }
}