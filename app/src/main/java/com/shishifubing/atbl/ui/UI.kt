package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

enum class LauncherNav {
    Home,
    Settings,
    AddWidget
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
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            LauncherScaffold(
                route = navBackStackEntry?.destination?.route,
                goBack = navController::popBackStack
            ) {
                LauncherNavGraph(navController = navController)
            }
        }
    }
}

@Composable
private fun LauncherNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
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
            SettingsRoute()
        }
        composable(route = LauncherNav.AddWidget.name) {
            AddWidgetScreen()
        }
    }
}