package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.shishifubing.atbl.LauncherNavigationState
import com.shishifubing.atbl.LauncherNavigator
import com.shishifubing.atbl.rememberNavigator

private val startDestination: LauncherRoute<*, *> = HomeRoute

object Routes {
    private val home = HomeRoute
    val addWidget = AddWidgetRoute
    val settings = SettingsRoute
    val shortcuts = EditSplitScreenShortcutsRoute
    val list = listOf(
        home, addWidget, settings, shortcuts
    )
}

@Composable
fun LauncherUi(modifier: Modifier = Modifier) {
    LauncherTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            val navController = rememberNavController()
            val navigator = rememberNavigator()
            var currentRoute = remember { startDestination }

            OnChangedNavState(
                navigator = navigator,
                navController = navController
            )

            LauncherScaffold(
                modifier = Modifier.safeDrawingPadding(),
                route = currentRoute,
                goBack = { navigator.popBackStack() }
            ) {
                LauncherNavGraph(navController) {
                    currentRoute = it
                }
            }
        }
    }
}

@Composable
private fun OnChangedNavState(
    navigator: LauncherNavigator,
    navController: NavHostController
) {
    val navState by navigator.navigationStateFlow.collectAsState()
    LaunchedEffect(key1 = navState) {
        when (navState) {
            is LauncherNavigationState.GoToRoute -> {
                val state = navState as LauncherNavigationState.GoToRoute
                val route = state.route
                navController.navigate(route = route.url) {
                    popUpTo(id = navController.graph.findStartDestination().id)
                    launchSingleTop = true
                }
            }

            LauncherNavigationState.Idle -> {}
            LauncherNavigationState.PopBackStack -> navController.popBackStack()
        }
        navigator.onNavigation(navState)
    }
}

@Composable
private fun LauncherNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onChangedRoute: (LauncherRoute<*, *>) -> Unit,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination.url
    ) {
        Routes.list.forEach { route ->
            launcherComposable(
                route = route,
                onChangedRoute = onChangedRoute
            )
        }
    }
}