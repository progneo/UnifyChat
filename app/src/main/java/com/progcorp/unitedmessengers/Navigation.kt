package com.progcorp.unitedmessengers

import androidx.annotation.StringRes
import androidx.navigation.NavBackStackEntry

sealed class Screen(val route: String, @StringRes val resourceId: Int, val iconId: Int) {

    object Telegram : Screen("telegram", R.string.telegram, R.drawable.ic_icon_telegram)

    object VK : Screen("vk", R.string.vk, R.drawable.ic_icon_vk)

    //object Chat : Screen("chat/{chatId}") {
    //    fun buildRoute(chatId: Long): String = "chat/${chatId}"
    //    fun getChatId(entry: NavBackStackEntry): Long =
    //        entry.arguments!!.getString("chatId")?.toLong()
    //            ?: throw IllegalArgumentException("chatId argument missing.")
    //}
//
    //object Dialog : Screen("dialog/{chatId}") {
    //    fun buildRoute(chatId: Long): String = "dialog/${chatId}"
    //    fun getChatId(entry: NavBackStackEntry): Long =
    //        entry.arguments!!.getString("chatId")?.toLong()
    //            ?: throw IllegalArgumentException("chatId argument missing.")
    //}
}
