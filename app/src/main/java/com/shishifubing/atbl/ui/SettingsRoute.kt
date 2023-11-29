package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    nav: LauncherNav,
    navController: NavController,
    vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val uiState by vm.uiState.collectAsState()
    ErrorToast(errorFlow = vm.error)
    LauncherScaffold(nav = nav, goBack = { navController.popBackStack() }) {
        when (uiState) {
            SettingsScreenUiState.Loading -> {
                PageLoadingIndicator(modifier = modifier)
            }

            is SettingsScreenUiState.Success -> (uiState as SettingsScreenUiState.Success).let {
                Column(modifier = modifier.verticalScroll(rememberScrollState())) {
                    SettingsGroupGeneral(it, vm.settingsActions)
                    SettingsGroupHiddenApps(it, vm.settingsActions)
                    SettingsGroupSplitScreen(it, vm.settingsActions)
                    SettingsGroupLayout(it, vm.settingsActions)
                    SettingsGroupAppCard(it, vm.settingsActions)
                }
            }
        }
    }
}






