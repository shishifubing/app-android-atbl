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
                        shortcutSeparator = it.settings.appCardSplitScreenSeparator,
                        removeShortcut = vm::removeSplitScreenShortcut,
                        addShortcut = vm::addSplitScreenShortcut,
                        setSeparator = vm::setSplitScreenShortcutSeparator
                    )
                    SettingsGroupLayout(
                        reverseOrder = it.settings.appLayoutReverseOrder,
                        setReverseOrder = vm::setAppLayoutReverseOrder,
                        horizontalPadding = it.settings.appLayoutHorizontalPadding,
                        setHorizontalPadding = vm::setAppLayoutHorizontalPadding,
                        verticalPadding = it.settings.appLayoutVerticalPadding,
                        setVerticalPadding = vm::setAppLayoutVerticalPadding,
                        horizontalArrangement = it.settings.appLayoutHorizontalArrangement,
                        setHorizontalArrangement = vm::setAppLayoutHorizontalArrangement,
                        verticalArrangement = it.settings.appLayoutVerticalArrangement,
                        setVerticalArrangement = vm::setAppLayoutVerticalArrangement,
                        sortBy = it.settings.appLayoutSortBy,
                        setSortBy = vm::setAppLayoutSortBy
                    )
                    SettingsGroupAppCard(
                        removeSpaces = it.settings.appCardLabelRemoveSpaces,
                        setRemoveSpaces = vm::setAppCardRemoveSpaces,
                        labelLowercase = it.settings.appCardLabelLowercase,
                        setLabelLowercase = vm::setAppCardLabelLowercase,
                        fontFamily = it.settings.appCardFontFamily,
                        setFontFamily = vm::setAppCardFontFamily,
                        textStyle = it.settings.appCardTextStyle,
                        setTextStyle = vm::setAppCardTextStyle,
                        textColor = it.settings.appCardTextColor,
                        setTextColor = vm::setAppCardTextColor,
                        padding = it.settings.appCardPadding,
                        setPadding = vm::setAppCardPadding
                    )
                }
            }
        }
    }
}






