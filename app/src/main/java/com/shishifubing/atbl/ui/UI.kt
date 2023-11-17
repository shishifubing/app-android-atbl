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
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = LauncherNav.Home.name
    ) {
        composable(route = LauncherNav.Home.name) {
            LauncherScreen(
                goToSettings = { navController.navigate(LauncherNav.Settings.name) },
                goToAddWidget = { navController.navigate(LauncherNav.AddWidget.name) }
            )
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