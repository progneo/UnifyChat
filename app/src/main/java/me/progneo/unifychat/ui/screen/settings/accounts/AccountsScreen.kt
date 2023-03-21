package me.progneo.unifychat.ui.screen.settings.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import me.progneo.unifychat.R
import me.progneo.unifychat.ui.components.DisplayText
import me.progneo.unifychat.ui.components.FeedbackIconButton
import me.progneo.unifychat.ui.components.UCScaffold
import me.progneo.unifychat.ui.icons.*
import me.progneo.unifychat.ui.screen.settings.SelectableSettingGroupItem
import me.progneo.unifychat.ui.theme.palette.onLight
import me.progneo.unifychat.util.NavDestinations
import me.progneo.unifychat.util.collectAsStateValue

@Composable
fun AccountsScreen(
    navController: NavHostController,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val accountUiState = viewModel.accountUiState.collectAsStateValue()

    LaunchedEffect(Unit) {
        viewModel.collectAccountsState()
    }

    UCScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface
            ) {
                navController.popBackStack()
            }
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(R.string.accounts), desc = "")
                }
                if (!accountUiState.isVkConnected) {
                    item {
                        SelectableSettingGroupItem(
                            title = stringResource(R.string.vk),
                            description = stringResource(R.string.login_text),
                            icon = Icons.VK,
                        ) {
                            navController.navigate(NavDestinations.VK_LOGIN)
                        }
                    }
                }
                else {
                    item {
                        SelectableSettingGroupItem(
                            title = stringResource(R.string.vk),
                            description = accountUiState.vkUsername,
                            icon = Icons.VK,
                        ) {
                            viewModel.logoutVk()
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewAccountsScreen() {
    val navController = rememberNavController()
    AccountsScreen(navController)
}