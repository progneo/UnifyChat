package me.progneo.unifychat.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import me.progneo.unifychat.R
import me.progneo.unifychat.ui.icons.Telegram
import me.progneo.unifychat.ui.icons.VK

sealed class BottomNavItem(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector,
) {
    object FavouriteScreen : BottomNavItem(
        route = "favourite",
        titleResId = R.string.favourite,
        icon = Icons.Default.Star,
    )

    object VKScreen : BottomNavItem(
        route = "search",
        titleResId = R.string.vk,
        icon = Icons.VK,
    )

    object TelegramScreen : BottomNavItem(
        route = "projfair",
        titleResId = R.string.telegram,
        icon = Icons.Telegram,
    )

    object SettingsScreen : BottomNavItem(
        route = "settings",
        titleResId = R.string.settings,
        icon = Icons.Default.Settings,
    )
}
