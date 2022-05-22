package com.progcorp.unitedmessengers.ui

import android.app.Activity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.progcorp.unitedmessengers.Screen
import com.progcorp.unitedmessengers.ui.home.MainScreen
import com.progcorp.unitedmessengers.ui.theme.UMTheme

@Composable
fun UMApp(activity: Activity) {
    UMTheme {
        activity.window.statusBarColor = MaterialTheme.colors.primaryVariant.toArgb()
        val navController = rememberNavController()
        MainNavHost(navController)
    }
}

@Composable
private fun MainNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            MainScreen(navController = navController, viewModel = hiltNavGraphViewModel(it))
        }
        composable(Screen.Chat.route) {
            val chatId = Screen.Chat.getChatId(it)
            val viewModel: ChatScreenViewModel =
                navController.hiltNavGraphViewModel(Screen.Chat.route)
            viewModel.setChatId(chatId)
            ChatScreen(
                chatId = chatId,
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(Screen.CreateChat.route) {
            CreateChatScreen(
                navigateUp = navController::navigateUp,
                viewModel = hiltNavGraphViewModel(it)
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(hiltNavGraphViewModel(it)) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
    }
}
