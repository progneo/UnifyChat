package me.progneo.unifychat.data.preference

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.map
import me.progneo.unifychat.util.collectAsStateValue
import me.progneo.unifychat.util.dataStore

data class Settings(

    val theme: ThemePreference = ThemePreference.default,
    val languages: LanguagesPreference = LanguagesPreference.default,
)

val LocalTheme =
    compositionLocalOf<ThemePreference> { ThemePreference.default }

val LocalLanguages =
    compositionLocalOf<LanguagesPreference> { LanguagesPreference.default }

@Composable
fun SettingsProvider(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val settings = remember {
        context.dataStore.data.map {
            Log.i("UCLog", "AppTheme: $it")
            it.toSettings()
        }
    }.collectAsStateValue(initial = Settings())

    CompositionLocalProvider(
        // Theme
        LocalTheme provides settings.theme,
        // Languages
        LocalLanguages provides settings.languages,
    ) {
        content()
    }
}
