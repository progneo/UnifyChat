package me.progneo.unifychat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import me.progneo.unifychat.ui.screen.home.favourite.FavouriteScreen
import me.progneo.unifychat.ui.screen.home.telegram.TelegramScreen
import me.progneo.unifychat.ui.screen.home.vk.VKScreen
import me.progneo.unifychat.ui.screen.settings.SettingsScreen

@Composable
fun BottomNavGraph(
    navController: NavHostController,
    bottomNavController: NavHostController,
) {
    NavHost(
        navController = bottomNavController,
        startDestination = BottomNavItem.FavouriteScreen.route,
    ) {
        composable(
            route = BottomNavItem.FavouriteScreen.route,
        ) {
            FavouriteScreen()
        }

        composable(
            route = BottomNavItem.VKScreen.route,
        ) {
            VKScreen()
        }

        composable(
            route = BottomNavItem.TelegramScreen.route,
        ) {
            TelegramScreen()
        }

        composable(
            route = BottomNavItem.SettingsScreen.route,
        ) {
            SettingsScreen(navController)
        }
    }
}
