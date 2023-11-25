package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsRoute(
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val uiState by vm.uiState.collectAsState()
    ErrorToast(errorFlow = vm.error)
    LauncherScaffold(screen = LauncherNav.Settings, goBack = goBack) {
        SettingsScreen(
            modifier = modifier,
            actions = vm.settingsActions,
            uiState = uiState
        )
    }
}


@Composable
private fun SettingsScreen(
    uiState: SettingsScreenUiState,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
) {
    if (uiState !is SettingsScreenUiState.Success) {
        return
    }
    Column(modifier = modifier) {
        SettingsGroupGeneral(uiState, actions)
        SettingsGroupHiddenApps(uiState, actions)
        SettingsGroupSplitScreen(uiState, actions)
        SettingsGroupLayout(uiState, actions)
        SettingsGroupAppCard(uiState, actions)
    }
}






