package com.shishifubing.atbl.ui

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.shishifubing.atbl.LauncherNavigationState
import com.shishifubing.atbl.data.UiState

interface LauncherRoute<State, VM : BaseViewModel<State>> {
    val url: String

    @get:StringRes
    val label: Int

    val showScaffold: Boolean

    @Composable
    fun Content(vm: VM, uiState: UiState.Success<State>)

    @Composable
    fun getViewModel(): VM
}

fun <State, VM : BaseViewModel<State>> NavGraphBuilder.launcherComposable(
    route: LauncherRoute<State, VM>,
    navController: NavHostController
) {
    composable(route = route.url) {
        val vm = route.getViewModel()
        val error by vm.errorFlow.collectAsState()
        val uiState by vm.uiStateFlow.collectAsState()
        val navigationState by vm.navigationStateFlow.collectAsState()

        OnChangedNavState(
            navState = navigationState,
            navController = navController,
            onNavigation = vm::onNavigation
        )
        ErrorToast(error = error)

        val content: @Composable () -> Unit = {
            when (uiState) {
                is UiState.Loading -> Unit

                is UiState.Success<State> -> route.Content(
                    vm = vm,
                    uiState = uiState as UiState.Success<State>
                )
            }
        }
        if (route.showScaffold) {
            LauncherScaffold(
                label = route.label,
                goBack = { vm.popBackStack() },
                content = content
            )
        } else {
            content()
        }
    }
}

@Composable
private fun OnChangedNavState(
    navState: LauncherNavigationState,
    onNavigation: (LauncherNavigationState) -> Unit,
    navController: NavHostController
) {
    LaunchedEffect(key1 = navState) {
        when (navState) {
            is LauncherNavigationState.GoToRoute -> {
                val route = navState.route
                navController.navigate(route = route.url) {
                    popUpTo(id = navController.graph.findStartDestination().id)
                    launchSingleTop = true
                }
            }

            LauncherNavigationState.Idle -> {}
            LauncherNavigationState.PopBackStack -> navController.popBackStack()
        }
        onNavigation(navState)
    }
}

@Composable
private fun ErrorToast(error: Throwable?) {
    val context = LocalContext.current
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
        }
    }
}