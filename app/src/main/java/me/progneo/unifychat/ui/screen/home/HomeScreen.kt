package me.progneo.unifychat.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import me.progneo.unifychat.ui.components.UCScaffold
import me.progneo.unifychat.ui.navigation.BottomNavBar
import me.progneo.unifychat.ui.navigation.BottomNavGraph

@Composable
fun HomeScreen(
    navController: NavHostController
) {
    val bottomNavController = rememberNavController()
    
    UCScaffold (
        content = {
            BottomNavGraph(
                navController = navController,
                bottomNavController = bottomNavController
            )
        },
        bottomBar = {
            BottomNavBar(bottomNavController)
        }

    )
}

@Composable
@Preview(showBackground = true)
fun PreviewHomeScreen() {
    HomeScreen(rememberNavController())
}