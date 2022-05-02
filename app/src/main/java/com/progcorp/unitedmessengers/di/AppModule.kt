package com.progcorp.unitedmessengers.di

import android.content.Context
import android.os.Build
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.TelegramClient
import com.progcorp.unitedmessengers.data.db.Conversations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.drinkless.td.libcore.telegram.TdApi
import java.util.*

object AppModule {
    fun provideIoDispatcher() = Dispatchers.IO

    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    fun provideTdlibParameters(context: Context): TdApi.TdlibParameters {
        return TdApi.TdlibParameters().apply {
            apiId = context.resources.getInteger(R.integer.telegram_api_id)
            apiHash = context.getString(R.string.telegram_api_hash)
            useMessageDatabase = true
            useSecretChats = true
            systemLanguageCode = Locale.getDefault().language
            databaseDirectory = context.filesDir.absolutePath
            deviceModel = Build.MODEL
            systemVersion = Build.VERSION.RELEASE
            applicationVersion = "0.1"
            enableStorageOptimizer = true
        }
    }
}