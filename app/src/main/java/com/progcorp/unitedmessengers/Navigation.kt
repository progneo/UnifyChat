package com.progcorp.unitedmessengers

import androidx.navigation.NavBackStackEntry

sealed class Screen(val route: String) {

    object Home : Screen("home")

    object Chat : Screen("chat/{chatId}") {
        fun buildRoute(chatId: Long): String = "chat/${chatId}"
        fun getChatId(entry: NavBackStackEntry): Long =
            entry.arguments!!.getString("chatId")?.toLong()
                ?: throw IllegalArgumentException("chatId argument missing.")
    }

    object Dialog : Screen("dialog/{chatId}") {
        fun buildRoute(chatId: Long): String = "dialog/${chatId}"
        fun getChatId(entry: NavBackStackEntry): Long =
            entry.arguments!!.getString("chatId")?.toLong()
                ?: throw IllegalArgumentException("chatId argument missing.")
    }
}
