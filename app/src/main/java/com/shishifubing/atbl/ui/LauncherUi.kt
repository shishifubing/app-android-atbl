package com.shishifubing.atbl.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shishifubing.atbl.R


sealed class LauncherNav(
    val route: String,
    @StringRes val label: Int
) {
    object Home : LauncherNav(
        route = "home_screen",
        label = R.string.navigation_home
    )

    object Settings : LauncherNav(
        route = "settings_screen",
        label = R.string.navigation_settings
    )

    object AddWidget : LauncherNav(
        route = "add_widget_screen",
        label = R.string.navigation_add_widget
    )
}

fun NavController.navigate(route: LauncherNav) = navigate(route.route) {
    popUpTo(graph.findStartDestination().id)
    launchSingleTop = true
}

@Composable
fun LauncherUi(modifier: Modifier = Modifier) {
    LauncherTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            LauncherNavGraph(modifier = Modifier.safeDrawingPadding())
        }
    }
}

@Composable
private fun LauncherNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = LauncherNav.Home.route
    ) {
        composable(route = LauncherNav.Home.route) {
            HomeRoute(
                navController = navController
            )
        }
        composable(route = LauncherNav.Settings.route) {
            SettingsRoute(
                navController = navController,
                nav = LauncherNav.Settings
            )
        }
        composable(route = LauncherNav.AddWidget.route) {
            AddWidgetScreen(
                navController = navController,
                nav = LauncherNav.AddWidget
            )
        }
    }
}