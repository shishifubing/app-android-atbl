package com.shishifubing.atbl.ui

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.shishifubing.atbl.data.UiState

interface LauncherRoute<S, T : BaseViewModel<S>> {
    val url: String

    @get:StringRes
    val label: Int
    val showScaffold: Boolean

    @Composable
    fun Content(vm: T, uiState: UiState.Success<S>)

    @Composable
    fun getViewModel(): T
}

fun <S, T : BaseViewModel<S>> NavGraphBuilder.launcherComposable(
    route: LauncherRoute<S, T>,
    onChangedRoute: (LauncherRoute<*, *>) -> Unit
) {
    composable(route = route.url) {
        LaunchedEffect(route) {
            onChangedRoute(route)
        }
        val vm = route.getViewModel()
        val error by vm.errorFlow.collectAsState()
        val uiState by vm.uiStateFlow.collectAsState()
        ErrorToast(error = error)
        when (uiState) {
            is UiState.Loading -> PageLoadingIndicator()
            is UiState.Success<S> -> route.Content(
                vm = vm,
                uiState = uiState as UiState.Success<S>
            )
        }
    }
}

@Composable
fun ErrorToast(error: Throwable?) {
    val context = LocalContext.current
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
        }
    }
}