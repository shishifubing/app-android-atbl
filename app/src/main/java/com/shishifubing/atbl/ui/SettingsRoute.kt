package com.shishifubing.atbl.ui

import android.os.ParcelFileDescriptor
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R
import com.shishifubing.atbl.data.UiState
import java.io.InputStream

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
        SettingsScreen(
            state = uiState.state,
            writeSettingsToFile = vm::writeSettingsToFile,
            updateSettingsFromStream = vm::updateSettingsFromStream,
            setPadding = vm::setAppCardPadding,
            setTextColor = vm::setAppCardTextColor,
            setTextStyle = vm::setAppCardTextStyle,
            setFontFamily = vm::setAppCardFontFamily,
            setSortBy = vm::setAppLayoutSortBy,
            setLabelLowercase = vm::setAppCardLabelLowercase,
            setVerticalArrangement = vm::setAppLayoutVerticalArrangement,
            setHorizontalArrangement = vm::setAppLayoutHorizontalArrangement,
            setVerticalPadding = vm::setAppLayoutVerticalPadding,
            setHorizontalPadding = vm::setAppLayoutHorizontalPadding,
            setSeparator = vm::setAppCardSplitScreenShortcutSeparator,
            setHiddenApps = vm::setHiddenApps,
            backupReset = vm::backupReset,
            setLabelRemoveSpaces = vm::setAppCardLabelRemoveSpaces,
            setReverseOrder = vm::setAppLayoutReverseOrder
        )
    }
}

@Composable
private fun SettingsScreen(
    modifier: Modifier = Modifier,
    state: Model.State,
    writeSettingsToFile: (() -> ParcelFileDescriptor?) -> Unit,
    backupReset: () -> Unit,
    updateSettingsFromStream: (() -> InputStream?) -> Unit,
    setHiddenApps: (List<String>) -> Unit,
    setSeparator: (String) -> Unit,
    setReverseOrder: (Boolean) -> Unit,
    setHorizontalPadding: (Int) -> Unit,
    setVerticalPadding: (Int) -> Unit,
    setHorizontalArrangement: (Model.Settings.HorizontalArrangement) -> Unit,
    setVerticalArrangement: (Model.Settings.VerticalArrangement) -> Unit,
    setSortBy: (Model.Settings.SortBy) -> Unit,
    setLabelLowercase: (Boolean) -> Unit,
    setLabelRemoveSpaces: (Boolean) -> Unit,
    setFontFamily: (Model.Settings.FontFamily) -> Unit,
    setTextStyle: (Model.Settings.TextStyle) -> Unit,
    setTextColor: (Model.Settings.TextColor) -> Unit,
    setPadding: (Int) -> Unit
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        SettingsGroupGeneral(
            writeSettingsToFile = writeSettingsToFile,
            backupReset = backupReset,
            updateSettingsFromStream = updateSettingsFromStream
        )
        SettingsGroupHiddenApps(
            apps = state.apps,
            setHiddenApps = setHiddenApps
        )
        SettingsGroupSplitScreen(
            shortcutSeparator = state.settings.appCard.splitScreenSeparator,
            setSeparator = setSeparator
        )
        SettingsGroupLayout(
            settings = state.settings.layout,
            setReverseOrder = setReverseOrder,
            setHorizontalPadding = setHorizontalPadding,
            setVerticalPadding = setVerticalPadding,
            setHorizontalArrangement = setHorizontalArrangement,
            setVerticalArrangement = setVerticalArrangement,
            setSortBy = setSortBy
        )
        SettingsGroupAppCard(
            settings = state.settings.appCard,
            setLabelLowercase = setLabelLowercase,
            setLabelRemoveSpaces = setLabelRemoveSpaces,
            setFontFamily = setFontFamily,
            setTextStyle = setTextStyle,
            setTextColor = setTextColor,
            setPadding = setPadding
        )
    }
}






