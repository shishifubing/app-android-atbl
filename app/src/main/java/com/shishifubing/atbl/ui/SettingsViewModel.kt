package com.shishifubing.atbl.ui


import android.os.ParcelFileDescriptor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.Defaults
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.data.SettingsScreenUIState
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
import java.io.InputStream


class SettingsViewModel(
    private val stateRepo: LauncherStateRepository
) : ViewModel() {
    companion object {
        val Factory = launcherViewModelFactory {
            SettingsViewModel(stateRepo = stateRepo)
        }
    }

    private val _errorFlow = MutableStateFlow<Throwable?>(null)
    val errorFlow = _errorFlow.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _errorFlow.update { e }
    }
    private var prevState = Defaults.State

    val uiState = stateRepo.observeState()
        .map { stateResult ->
            val state = stateResult.fold(
                onSuccess = {
                    prevState = it
                    it
                },
                onFailure = {
                    _errorFlow.update { it }
                    prevState
                }
            )
            SettingsScreenUIState.Success(state = state)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SettingsScreenUIState.Loading
        )


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

    fun updateSettingsFromStream(getStream: () -> InputStream?) {
        stateAction {
            getStream()?.use { stream ->
                this.updateSettingsFromInputStream(stream)
            }
        }
    }

    fun writeSettingsToFile(getFile: () -> ParcelFileDescriptor?) {
        stateAction {
            getFile()?.use { file ->
                FileOutputStream(file.fileDescriptor).use { stream ->
                    this.writeSettingsToOutputStream(stream)
                }
            }
        }
    }

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) { action() }
    }

    private fun stateAction(action: suspend LauncherStateRepository.() -> Unit) {
        launch { action.invoke(stateRepo) }
    }

    private fun updateAppCard(action: Model.Settings.AppCard.Builder.() -> Unit) {
        stateAction {
            updateSettings {
                appCard = appCard.toBuilder().apply(action).build()
            }
        }
    }

    private fun updateLayout(action: Model.Settings.Layout.Builder.() -> Unit) {
        stateAction {
            updateSettings {
                layout = layout.toBuilder().apply(action).build()
            }
        }
    }
}