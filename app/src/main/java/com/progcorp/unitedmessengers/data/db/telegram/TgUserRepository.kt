package com.progcorp.unitedmessengers.data.db.telegram

import com.progcorp.unitedmessengers.App
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.drinkless.td.libcore.telegram.TdApi

@ExperimentalCoroutinesApi
class TgUserRepository {
    fun getUser(userId: Long?): Flow<TdApi.User> = callbackFlow {
        if (userId == null) {
            App.application.tgClient.client.send(TdApi.GetUser()) {
                trySend((it as TdApi.User)).isSuccess
            }
        }
        else {
            App.application.tgClient.client.send(TdApi.GetUser(userId)) {
                trySend((it as TdApi.User)).isSuccess
            }
        }
        awaitClose { }
    }
}