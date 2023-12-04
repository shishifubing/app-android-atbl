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
    LauncherScaffold(
        modifier = modifier,
        nav = nav,
        goBack = { navController.popBackStack() }
    ) {
        when (uiState) {
            SettingsScreenUIState.Loading -> PageLoadingIndicator()

            is SettingsScreenUIState.Success -> {
                val state = (uiState as SettingsScreenUIState.Success).state
                val apps = state.apps
                val settings = state.settings
                val shortcuts = state.splitScreenShortcuts
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    SettingsGroupGeneral(
                        writeSettings = vm::writeSettings,
                        backupReset = vm::backupReset,
                        updateSettingsFromBytes = vm::updateSettingsFromBytes
                    )
                    SettingsGroupHiddenApps(
                        apps = apps,
                        setHiddenApps = vm::setHiddenApps
                    )
                    SettingsGroupSplitScreen(
                        apps = apps,
                        splitScreenShortcuts = shortcuts,
                        shortcutSeparator = settings.appCard.splitScreenSeparator,
                        removeShortcut = vm::removeSplitScreenShortcut,
                        addShortcut = vm::addSplitScreenShortcut,
                        setSeparator = vm::setAppCardSplitScreenShortcutSeparator
                    )
                    SettingsGroupLayout(
                        settings = settings.layout,
                        setReverseOrder = vm::setAppLayoutReverseOrder,
                        setHorizontalPadding = vm::setAppLayoutHorizontalPadding,
                        setVerticalPadding = vm::setAppLayoutVerticalPadding,
                        setHorizontalArrangement = vm::setAppLayoutHorizontalArrangement,
                        setVerticalArrangement = vm::setAppLayoutVerticalArrangement,
                        setSortBy = vm::setAppLayoutSortBy
                    )
                    SettingsGroupAppCard(
                        settings = settings.appCard,
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






