package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


sealed class LauncherNav(val route: String) {
    object Home : LauncherNav(route = "home_screen")
    object Settings : LauncherNav(route = "settings_screen")
    object AddWidget : LauncherNav(route = "add_widget_screen")
}

@Composable
fun UI(
    modifier: Modifier = Modifier,
) {
    LauncherTheme {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            val navController = rememberNavController()
            LauncherNavGraph(navController = navController)
        }
    }
}

@Composable
private fun LauncherNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = LauncherNav.Home.route
    ) {
        composable(route = LauncherNav.Home.route) {
            LauncherRoute(navController = navController)
        }
        composable(route = LauncherNav.Settings.route) {
            SettingsRoute(navController = navController)
        }
        composable(route = LauncherNav.AddWidget.route) {
            AddWidgetScreen(navController = navController)
        }
    }
}