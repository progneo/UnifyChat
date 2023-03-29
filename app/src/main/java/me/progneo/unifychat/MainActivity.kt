package me.progneo.unifychat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.progneo.unifychat.data.preference.LanguagesPreference
import me.progneo.unifychat.data.preference.SettingsProvider
import me.progneo.unifychat.ui.navigation.NavGraph
import me.progneo.unifychat.ui.theme.UCTheme
import me.progneo.unifychat.util.languages

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        LanguagesPreference.fromValue(languages).let {
            if (it == LanguagesPreference.UseDeviceLanguages) return@let
            it.setLocale(this)
        }

        setContent {
            SettingsProvider {
                UCTheme {
                    Surface {
                        val navController = rememberNavController()
                        NavGraph(navController)
                    }
                }
            }
        }
    }
}
