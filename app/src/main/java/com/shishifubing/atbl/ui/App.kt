package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

enum class LauncherNav {
    Home,
    Settings
}

@Composable
fun App(
    vmLauncher: LauncherViewModel,
    vmSettings: SettingsViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = LauncherNav.Home.name
    ) {
        composable(route = LauncherNav.Home.name) {
            LauncherScreen(
                vm = vmLauncher,
                goToSettings = { navController.navigate(LauncherNav.Settings.name) }
            )
        }
        composable(route = LauncherNav.Settings.name) {
            SettingsScreen(
                vm = vmSettings,
                goBack = { navController.popBackStack() }
            )
        }
    }
}