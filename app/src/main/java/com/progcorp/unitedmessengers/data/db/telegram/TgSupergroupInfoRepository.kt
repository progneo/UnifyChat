package com.progcorp.unitedmessengers.data.db.telegram

import android.util.Log
import com.progcorp.unitedmessengers.App
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.TdApi

@OptIn(ExperimentalCoroutinesApi::class)
class TgSupergroupInfoRepository {

    fun getSupergroupInfo(chatId: Long): Flow<TdApi.SupergroupFullInfo> =
        callbackFlow {
            App.application.tgClient.client.send(TdApi.GetSupergroupFullInfo(chatId)) {
                when (it.constructor) {
                    TdApi.SupergroupFullInfo.CONSTRUCTOR -> {
                        trySend(it as TdApi.SupergroupFullInfo).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        Log.e("getChats", (it as TdApi.Error).message)
                    }
                    else -> {
                        Log.e("getBasicGroup", "Something went wrong")
                    }
                }
            }
            awaitClose { }
        }

}