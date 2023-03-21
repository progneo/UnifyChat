package me.progneo.unifychat.data.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope

sealed class Preference {

    abstract fun put(context: Context, scope: CoroutineScope)
}

fun Preferences.toSettings(): Settings {
    return Settings(
        // Theme
        theme = ThemePreference.fromPreferences(this),
        // Languages
        languages = LanguagesPreference.fromPreferences(this),
    )
}