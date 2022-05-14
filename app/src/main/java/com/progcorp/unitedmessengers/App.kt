package com.progcorp.unitedmessengers

import android.app.Application
import android.os.Build
import com.progcorp.unitedmessengers.data.TelegramClient
import com.progcorp.unitedmessengers.util.VKAccountService
import com.progcorp.unitedmessengers.interfaces.IAccountService
import org.drinkless.td.libcore.telegram.TdApi
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*

class App : Application() {
    lateinit var vkAccountService: IAccountService
    lateinit var vkRetrofit: Retrofit
    lateinit var tgClient: TelegramClient

    companion object {
        lateinit var application: App
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        setLocale()
        tgClient = TelegramClient(TdApi.TdlibParameters().apply {
            apiId = applicationContext.resources.getInteger(R.integer.telegram_api_id)
            apiHash = applicationContext.getString(R.string.telegram_api_hash)
            useMessageDatabase = true
            useSecretChats = true
            systemLanguageCode = Locale.getDefault().language
            databaseDirectory = applicationContext.filesDir.absolutePath
            deviceModel = Build.MODEL
            systemVersion = Build.VERSION.RELEASE
            applicationVersion = "0.1"
            enableStorageOptimizer = true
        })
        vkAccountService = VKAccountService(getSharedPreferences("vk_account", MODE_PRIVATE))
        vkRetrofit = Retrofit.Builder()
            .baseUrl("https://api.vk.com/method/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
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