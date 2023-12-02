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
                    SettingsGroupGeneral(
                        settings = it.settings,
                        backupReset = vm::backupReset,
                        updateSettingsFromBytes = vm::updateSettingsFromBytes
                    )
                    SettingsGroupHiddenApps(
                        apps = it.apps,
                        setHiddenApps = vm::setHiddenApps
                    )
                    SettingsGroupSplitScreen(
                        apps = it.apps,
                        splitScreenShortcuts = it.splitScreenShortcuts,
                        shortcutSeparator = it.settings.appCard.model.splitScreenSeparator,
                        removeShortcut = vm::removeSplitScreenShortcut,
                        addShortcut = vm::addSplitScreenShortcut,
                        setSeparator = vm::setAppCardSplitScreenShortcutSeparator
                    )
                    SettingsGroupLayout(
                        settings = it.settings.layout,
                        setReverseOrder = vm::setAppLayoutReverseOrder,
                        setHorizontalPadding = vm::setAppLayoutHorizontalPadding,
                        setVerticalPadding = vm::setAppLayoutVerticalPadding,
                        setHorizontalArrangement = vm::setAppLayoutHorizontalArrangement,
                        setVerticalArrangement = vm::setAppLayoutVerticalArrangement,
                        setSortBy = vm::setAppLayoutSortBy
                    )
                    SettingsGroupAppCard(
                        settings = it.settings.appCard,
                        setLabelLowercase = vm::setAppCardLabelLowercase,
                        setLabelRemoveSpaces = vm::setAppCardLabelRemoveSpaces,
                        setFontFamily = vm::setAppCardFontFamily,
                        setTextStyle = vm::setAppCardTextStyle,
                        setTextColor = vm::setAppCardTextColor,
                        setPadding = vm::setAppCardPadding
                    )
                }
            }
        }
    }
}






