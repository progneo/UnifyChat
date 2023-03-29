package me.progneo.unifychat.ui.screen.settings

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import me.progneo.unifychat.R
import me.progneo.unifychat.data.preference.LocalLanguages
import me.progneo.unifychat.data.preference.LocalTheme
import me.progneo.unifychat.ui.components.DisplayText
import me.progneo.unifychat.ui.components.UCScaffold
import me.progneo.unifychat.util.NavDestinations

@Composable
fun SettingsScreen(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val languages = LocalLanguages.current
    val theme = LocalTheme.current

    UCScaffold(
        content = {
            LazyColumn {
                item {
                    DisplayText(
                        text = remember(configuration.locales) {
                            context.resources.getString(R.string.settings)
                        },
                        desc = "",
                    )
                }
                item {
                    SelectableSettingGroupItem(
                        title = stringResource(R.string.accounts),
                        description = stringResource(R.string.accounts_description),
                        icon = Icons.Outlined.AccountCircle,
                    ) {
                        navController.navigate(NavDestinations.ACCOUNTS)
                    }
                }
            }
        },
    )
}

@Composable
@Preview(showBackground = true)
fun SettingsScreenPreview() {
    SettingsScreen(rememberNavController())
}
