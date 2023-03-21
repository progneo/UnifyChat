package me.progneo.unifychat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import me.progneo.unifychat.ui.screen.home.HomeScreen
import me.progneo.unifychat.ui.screen.settings.accounts.AccountsScreen
import me.progneo.unifychat.ui.screen.settings.vk.login.LoginVkScreen
import me.progneo.unifychat.util.NavDestinations

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = NavDestinations.HOME_SCREEN
    ) {
        composable(
            route = NavDestinations.HOME_SCREEN
        ) {
            HomeScreen(navController)
        }

        composable(
            route = NavDestinations.ACCOUNTS
        ) {
            AccountsScreen(navController)
        }

        composable(
            route = NavDestinations.VK_LOGIN
        ) {
            LoginVkScreen(navController)
        }
    }
}