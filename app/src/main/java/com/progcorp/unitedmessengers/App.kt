package com.progcorp.unitedmessengers

import android.app.Application
import android.os.Build
import com.google.android.material.color.DynamicColors
import com.progcorp.unitedmessengers.data.clients.TelegramClient
import com.progcorp.unitedmessengers.data.clients.VKClient
import com.progcorp.unitedmessengers.data.db.TelegramDataSource
import com.progcorp.unitedmessengers.data.db.VKDataSource
import com.progcorp.unitedmessengers.data.db.VKRepository
import com.progcorp.unitedmessengers.interfaces.IAccountService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*

class App : Application() {
    companion object {
        lateinit var application: App
    }

    lateinit var vkClient: VKClient
    lateinit var tgClient: TelegramClient

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