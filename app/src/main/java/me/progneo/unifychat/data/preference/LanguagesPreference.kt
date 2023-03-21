package me.progneo.unifychat.data.preference

import android.content.Context
import android.os.LocaleList
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.progneo.unifychat.R
import me.progneo.unifychat.util.DataStoreKeys
import me.progneo.unifychat.util.dataStore
import me.progneo.unifychat.util.put
import java.util.*

sealed class LanguagesPreference(val value: Int) : Preference() {

    object UseDeviceLanguages : LanguagesPreference(0)
    object English : LanguagesPreference(1)
    object Russian : LanguagesPreference(2)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.Languages,
                value
            )
            setLocale(context)
        }
    }

    fun toDescription(context: Context): String =
        when (this) {
            UseDeviceLanguages -> context.getString(R.string.use_device_language)
            Russian -> context.getString(R.string.russian)
            English -> context.getString(R.string.english)
        }

    fun getLocale(): Locale =
        when (this) {
            UseDeviceLanguages -> LocaleList.getDefault().get(0)
            Russian -> Locale("ru", "RU")
            English -> Locale("en", "US")
        }

    fun setLocale(context: Context) {
        val locale = getLocale()
        val resources = context.resources
        val metrics = resources.displayMetrics
        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLocales(LocaleList(locale))
        context.createConfigurationContext(configuration)
        resources.updateConfiguration(configuration, metrics)

        val appResources = context.applicationContext.resources
        val appMetrics = appResources.displayMetrics
        val appConfiguration = appResources.configuration
        appConfiguration.setLocale(locale)
        appConfiguration.setLocales(LocaleList(locale))
        context.applicationContext.createConfigurationContext(appConfiguration)
        appResources.updateConfiguration(appConfiguration, appMetrics)
    }

    companion object {

        val default = UseDeviceLanguages
        val values = listOf(
            UseDeviceLanguages,
            English,
            Russian
        )

        fun fromPreferences(preferences: Preferences): LanguagesPreference =
            when (preferences[DataStoreKeys.Languages.key]) {
                0 -> UseDeviceLanguages
                1 -> English
                2 -> Russian
                else -> default
            }

        fun fromValue(value: Int): LanguagesPreference =
            when (value) {
                0 -> UseDeviceLanguages
                1 -> English
                2 -> Russian
                else -> default
            }
    }

}
