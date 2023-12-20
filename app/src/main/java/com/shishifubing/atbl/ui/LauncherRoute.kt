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
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.data.UiState

interface LauncherRoute<T : BaseViewModel> {
    val url: String

    @get:StringRes
    val label: Int

    val showScaffold: Boolean

    @Composable
    fun Content(vm: T, uiState: UiState.Success<Model.State>)

    @Composable
    fun getViewModel(): T
}

fun <T : BaseViewModel> NavGraphBuilder.launcherComposable(
    route: LauncherRoute<T>,
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

        LauncherScaffold(
            label = route.label,
            goBack = { vm.popBackStack() },
            showTopAppBar = route.showScaffold
        ) {
            when (uiState) {
                is UiState.Loading -> Unit

                is UiState.Success<Model.State> -> route.Content(
                    vm = vm,
                    uiState = uiState as UiState.Success<Model.State>
                )
            }
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