package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

enum class LauncherNav {
    Home,
    Settings,
    AddWidget
}

@Composable
fun UI(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val goBack: () -> Unit = { navController.popBackStack() }
    val navigate: (LauncherNav) -> Unit = { navController.navigate(it.name) }
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = LauncherNav.Home.name
    ) {
        composable(route = LauncherNav.Home.name) {
            LauncherRoute(navigate = navigate)
        }
        composable(route = LauncherNav.Settings.name) {
            LauncherScaffold(screen = LauncherNav.Settings, goBack = goBack) {
                SettingsScreen()
            }
        }
        composable(route = LauncherNav.AddWidget.name) {
            LauncherScaffold(screen = LauncherNav.AddWidget, goBack = goBack) {
                AddWidgetScreen()
            }
        }
    }
}