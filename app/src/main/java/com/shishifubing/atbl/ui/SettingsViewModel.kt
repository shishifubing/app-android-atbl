package com.shishifubing.atbl.ui


import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherFontFamily
import com.shishifubing.atbl.LauncherHorizontalArrangement
import com.shishifubing.atbl.LauncherSettings
import com.shishifubing.atbl.LauncherSortBy
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.LauncherTextStyle
import com.shishifubing.atbl.LauncherVerticalArrangement
import com.shishifubing.atbl.launcherViewModelFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class SettingsViewModel(
    private val stateRepo: LauncherStateRepository
) : ViewModel() {
    companion object {
        val Factory = launcherViewModelFactory {
            SettingsViewModel(stateRepo = stateRepo)
        }
    }

    private val _error = MutableStateFlow<Throwable?>(null)
    val error = _error.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _error.update { e }
    }

    val uiState = stateRepo.observeState().map { state ->
        SettingsScreenUiState.Success(
            apps = state.appsMap.values,
            splitScreenShortcuts = state.splitScreenShortcutsMap.values.sortedBy { it.appTop.packageName },
            settings = state.settings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsScreenUiState.Loading
    )


    fun updateSettingsFromBytes(bytes: ByteArray) {
        stateAction { updateSettings { mergeFrom(bytes) } }
    }

    fun backupReset() {
        launch { stateRepo.resetSettings() }
    }

    fun setHiddenApps(packageNames: List<String>) {
        stateAction { setHiddenApps(packageNames) }
    }

    fun setSplitScreenShortcutSeparator(value: String) {
        updateSettings { appCardSplitScreenSeparator = value }
    }

    fun removeSplitScreenShortcut(shortcut: LauncherSplitScreenShortcut) {
        stateAction { removeSplitScreenShortcut(shortcut) }
    }

    fun addSplitScreenShortcut(appTop: LauncherApp, appBottom: LauncherApp) {
        stateAction {
            addSplitScreenShortcut(appTop.packageName, appBottom.packageName)
        }
    }

    fun setAppLayoutReverseOrder(value: Boolean) {
        updateSettings { appLayoutReverseOrder = value }
    }

    fun setAppLayoutHorizontalPadding(value: Int) {
        updateSettings { appLayoutHorizontalPadding = value }
    }

    fun setAppLayoutVerticalPadding(value: Int) {
        updateSettings { appLayoutVerticalPadding = value }
    }

    fun setAppLayoutHorizontalArrangement(value: LauncherHorizontalArrangement) {
        updateSettings { appLayoutHorizontalArrangement = value }
    }

    fun setAppLayoutVerticalArrangement(value: LauncherVerticalArrangement) {
        updateSettings { appLayoutVerticalArrangement = value }
    }

    fun setAppLayoutSortBy(value: LauncherSortBy) {
        updateSettings { appLayoutSortBy = value }
    }

    fun setAppCardRemoveSpaces(value: Boolean) {
        updateSettings { appCardLabelRemoveSpaces = value }
    }

    fun setAppCardLabelLowercase(value: Boolean) {
        updateSettings { appCardLabelRemoveSpaces = value }
    }

    fun setAppCardFontFamily(value: LauncherFontFamily) {
        updateSettings { appCardFontFamily = value }
    }

    fun setAppCardTextStyle(value: LauncherTextStyle) {
        updateSettings { appCardTextStyle = value }
    }

    fun setAppCardTextColor(value: LauncherTextColor) {
        updateSettings { appCardTextColor = value }
    }

    fun setAppCardPadding(value: Int) {
        updateSettings { appCardPadding = value }
    }

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) { action() }
    }

    private fun stateAction(action: suspend LauncherStateRepository.() -> Unit) {
        launch { action.invoke(stateRepo) }
    }

    private fun updateSettings(action: LauncherSettings.Builder.() -> Unit) {
        stateAction {
            updateSettings(action)
        }
    }
}

@Stable
sealed interface SettingsScreenUiState {
    data class Success(
        val settings: LauncherSettings,
        val apps: Collection<LauncherApp>,
        val splitScreenShortcuts: List<LauncherSplitScreenShortcut>
    ) : SettingsScreenUiState

    object Loading : SettingsScreenUiState
}