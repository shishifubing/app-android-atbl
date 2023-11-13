package com.shishifubing.atbl.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shishifubing.atbl.R

sealed class LauncherNav(
    val route: String,
    val icon: ImageVector,
    @StringRes val resourceId: Int
) {
    object Home : LauncherNav(
        "home", Icons.Filled.Home, R.string.route_home_label
    )

    object Settings : LauncherNav(
        "settings", Icons.Filled.Settings, R.string.route_settings_label
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    vm: LauncherViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val navItems = listOf(LauncherNav.Home, LauncherNav.Settings)
    Scaffold(
        modifier = modifier,
        topBar = {
            if (navController.previousBackStackEntry == null) {
                return@Scaffold
            }
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_settings),
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Arrow back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEach { navItem ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                navItem.icon,
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(navItem.resourceId)) },
                        selected = backStackEntry
                            ?.destination
                            ?.hierarchy
                            ?.any { it.route == navItem.route } == true,
                        onClick = {
                            navController.navigate(navItem.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = LauncherNav.Home.route
            ) {
                composable(route = LauncherNav.Home.route) {
                    LauncherScreen(
                        vm = vm
                    )
                }
                composable(route = LauncherNav.Settings.route) {
                    SettingsScreen(
                        vm = vm
                    )
                }
            }
        }
    }
}