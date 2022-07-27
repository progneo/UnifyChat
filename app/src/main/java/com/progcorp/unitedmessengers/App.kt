package com.progcorp.unitedmessengers

import android.app.Application
import android.os.Build
import androidx.lifecycle.MediatorLiveData
import com.google.android.material.color.DynamicColors
import com.progcorp.unitedmessengers.data.clients.TelegramClient
import com.progcorp.unitedmessengers.data.clients.VKClient
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessengers.data.model.MessageText
import com.progcorp.unitedmessengers.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi
import java.util.*

class App : Application() {
    companion object {
        lateinit var application: App
    }

    lateinit var vkClient: VKClient
    lateinit var tgClient: TelegramClient
    val mailingList = MediatorLiveData<MutableList<Conversation>>()

    override fun onCreate() {
        super.onCreate()
        setLocale()
        application = this

        vkClient = VKClient(getSharedPreferences("vk_account", MODE_PRIVATE))
        tgClient = TelegramClient(TdApi.TdlibParameters().apply {
            apiId = applicationContext.resources.getInteger(R.integer.telegram_api_id)
            apiHash = applicationContext.getString(R.string.telegram_api_hash)
            useMessageDatabase = true
            useSecretChats = true
            systemLanguageCode = Locale.getDefault().language
            databaseDirectory = applicationContext.filesDir.absolutePath
            deviceModel = Build.MODEL
            systemVersion = Build.VERSION.RELEASE
            applicationVersion = "1.3.0"
            enableStorageOptimizer = true
        })

        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    fun startMailing(messageText: MessageText) {
        val vkMessage = Message(
            id = 0,
            timeStamp = Date().time,
            sender = vkClient.user.value,
            isOutgoing = true,
            replyToMessage = null,
            content = messageText,
            canBeEdited = true,
            canBeDeletedForAllUsers = true,
            canBeDeletedOnlyForSelf = true
        )
        val tgMessage = Message(
            id = 0,
            timeStamp = Date().time,
            sender = tgClient.user.value,
            isOutgoing = true,
            replyToMessage = null,
            content = messageText,
            canBeEdited = true,
            canBeDeletedForAllUsers = true,
            canBeDeletedOnlyForSelf = true
        )
        MainScope().launch(Dispatchers.IO) {
            mailingList.value?.let { list ->
                for (conversation in list) {
                    when (conversation.messenger) {
                        Constants.Messenger.TG -> {
                            tgClient.repository.sendMessage(conversation.id, tgMessage).first()
                        }
                        Constants.Messenger.VK -> {
                            vkClient.repository.sendMessage(conversation.id, vkMessage).first()
                            delay(1000)
                        }
                    }
                }
            }
        }
    }


    private fun setLocale() {
        val config = resources.configuration
        if (Locale.getDefault().displayLanguage == "Russia") {
            val locale = Locale("ru")
            Locale.setDefault(locale)
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }
}