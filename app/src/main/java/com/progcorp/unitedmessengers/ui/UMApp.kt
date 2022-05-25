package com.progcorp.unitedmessengers.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.progcorp.unitedmessengers.Screen
import com.progcorp.unitedmessengers.ui.telegram.Telegram
import com.progcorp.unitedmessengers.ui.theme.UMTheme
import com.progcorp.unitedmessengers.ui.vk.VK

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UMApp() {
    UMTheme {
        val systemUiController = rememberSystemUiController()
        val darkIcons = !isSystemInDarkTheme()
        val navController = rememberNavController()
        SideEffect {
            systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = darkIcons)
        }
        Scaffold(bottomBar = {
            BottomNavigation(navController, Modifier.navigationBarsPadding().imePadding())
        }) {
            innerPadding -> MainNavHost(navController = navController, innerPadding = innerPadding)
        }
    }
}

@Composable
private fun MainNavHost(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(navController, startDestination = Screen.VK.route, Modifier.padding(innerPadding)) {
        composable(Screen.Telegram.route) { Telegram() }
        composable(Screen.VK.route) { VK(navigateToConversation = {}) }
    }
}

@Composable
fun BottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf(
        Screen.Telegram,
        Screen.VK
    )
    Surface(tonalElevation = 16.dp) {
        NavigationBar (modifier = modifier) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            items.forEachIndexed() { index, item ->
                NavigationBarItem(
                    icon = { Icon(painter = painterResource(id = item.iconId), contentDescription = null, modifier = Modifier.size(25.dp)) },
                    label = { Text(stringResource(item.resourceId)) },
                    selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        selectedItem = index
                    },
                    alwaysShowLabel = true,
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewBottomNavigation() {
    BottomNavigation(rememberNavController())
}