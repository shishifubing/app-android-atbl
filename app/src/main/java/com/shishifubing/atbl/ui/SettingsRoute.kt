package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val uiState by vm.uiState.collectAsState()
    ErrorToast(errorFlow = vm.error)
    when (uiState) {
        SettingsScreenUiState.Loading -> LauncherPageLoadingIndicator()

        is SettingsScreenUiState.Success -> (uiState as SettingsScreenUiState.Success).let {
            Column(modifier = modifier) {
                SettingsGroupGeneral(it, vm.settingsActions)
                SettingsGroupHiddenApps(it, vm.settingsActions)
                SettingsGroupSplitScreen(it, vm.settingsActions)
                SettingsGroupLayout(it, vm.settingsActions)
                SettingsGroupAppCard(it, vm.settingsActions)
            }
        }
    }
}






