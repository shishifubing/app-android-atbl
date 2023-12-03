package com.shishifubing.atbl.ui


import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.Model
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
import java.io.FileOutputStream


@Immutable
sealed interface SettingsScreenUIState {
    @Immutable
    data class Success(val state: Model.State) : SettingsScreenUIState

    @Immutable
    data object Loading : SettingsScreenUIState
}

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

    val uiState = stateRepo.observeState()
        .map { SettingsScreenUIState.Success(state = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SettingsScreenUIState.Loading
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

    fun removeSplitScreenShortcut(shortcut: Model.SplitScreenShortcut) {
        stateAction { removeSplitScreenShortcut(shortcut) }
    }

    fun addSplitScreenShortcut(firstApp: Model.App, secondApp: Model.App) {
        stateAction {
            addSplitScreenShortcut(firstApp.packageName, secondApp.packageName)
        }
    }

    fun setAppLayoutReverseOrder(value: Boolean) {
        updateLayout { reverseOrder = value }
    }

    fun setAppLayoutHorizontalPadding(value: Int) {
        updateLayout { horizontalPadding = value }
    }

    fun setAppLayoutVerticalPadding(value: Int) {
        updateLayout { verticalPadding = value }
    }

    fun setAppLayoutHorizontalArrangement(value: Model.Settings.HorizontalArrangement) {
        updateLayout { horizontalArrangement = value }
    }

    fun setAppLayoutVerticalArrangement(value: Model.Settings.VerticalArrangement) {
        updateLayout { verticalArrangement = value }
    }

    fun setAppLayoutSortBy(value: Model.Settings.SortBy) {
        updateLayout { sortBy = value }
    }

    fun setAppCardLabelRemoveSpaces(value: Boolean) {
        updateAppCard { labelRemoveSpaces = value }
    }

    fun setAppCardLabelLowercase(value: Boolean) {
        updateAppCard { labelRemoveSpaces = value }
    }

    fun setAppCardFontFamily(value: Model.Settings.FontFamily) {
        updateAppCard { fontFamily = value }
    }

    fun setAppCardTextStyle(value: Model.Settings.TextStyle) {
        updateAppCard { textStyle = value }
    }

    fun setAppCardTextColor(value: Model.Settings.TextColor) {
        updateAppCard { textColor = value }
    }

    fun setAppCardPadding(value: Int) {
        updateAppCard { padding = value }
    }

    fun setAppCardSplitScreenShortcutSeparator(value: String) {
        updateAppCard { splitScreenSeparator = value }
    }

    fun writeSettings(stream: FileOutputStream) {
        stateAction { writeSettings(stream) }
    }

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) { action() }
    }

    private fun stateAction(action: suspend LauncherStateRepository.() -> Unit) {
        launch { action.invoke(stateRepo) }
    }

    private fun updateSettings(action: Model.Settings.Builder.() -> Unit) {
        stateAction {
            updateSettings(action)
        }
    }

    private fun updateAppCard(action: Model.Settings.AppCard.Builder.() -> Unit) {
        updateSettings {
            appCard = appCard.toBuilder().apply(action).build()
        }
    }

    private fun updateLayout(action: Model.Settings.Layout.Builder.() -> Unit) {
        updateSettings {
            layout = layout.toBuilder().apply(action).build()
        }
    }
}