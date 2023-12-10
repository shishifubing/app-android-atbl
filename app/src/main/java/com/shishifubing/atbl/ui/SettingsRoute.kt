package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R
import com.shishifubing.atbl.data.UiState

object SettingsRoute : LauncherRoute<Model.State, SettingsViewModel> {
    override val url = "settings_screen"
    override val label = R.string.navigation_settings
    override val showScaffold = true

    @Composable
    override fun getViewModel(): SettingsViewModel {
        return viewModel(factory = SettingsViewModel.Factory)
    }

    @Composable
    override fun Content(
        vm: SettingsViewModel,
        uiState: UiState.Success<Model.State>
    ) {
        SettingsScreen(vm = vm, state = uiState.state)
    }
}

@Composable
private fun SettingsScreen(
    modifier: Modifier = Modifier,
    vm: SettingsViewModel,
    state: Model.State
) {
    val apps = state.apps
    val settings = state.settings
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        SettingsGroupGeneral(
            writeSettingsToFile = vm::writeSettingsToFile,
            backupReset = vm::backupReset,
            updateSettingsFromStream = vm::updateSettingsFromStream
        )
        SettingsGroupHiddenApps(
            apps = apps,
            setHiddenApps = vm::setHiddenApps
        )
        SettingsGroupSplitScreen(
            shortcutSeparator = settings.appCard.splitScreenSeparator,
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






