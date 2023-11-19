package com.shishifubing.atbl.ui


import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherApplication
import com.shishifubing.atbl.LauncherHorizontalArrangement
import com.shishifubing.atbl.LauncherSettings
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.domain.LauncherAppsRepository
import com.shishifubing.atbl.domain.LauncherSettingsRepository
import com.shishifubing.atbl.domain.LauncherSettingsSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingsField<T>(
    private val flow: Flow<T>,
    val setter: (T) -> Unit
) {
    @Composable
    fun collectAsState(): State<T> {
        return flow.collectAsState(runBlocking { flow.first() })
    }
}

class SettingsViewModel(
    private val settingsRepo: LauncherSettingsRepository,
    private val appsRepo: LauncherAppsRepository,
    val initialSettings: LauncherSettings
) : ViewModel() {

    val settingsFlow = settingsRepo.settingsFlow
    val shortcutsFlow = appsRepo.appsFlow.map { it.splitScreenShortcutsList }
    val appsFlow = appsRepo.appsFlow.map { it.appsMap.values }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as LauncherApplication
                SettingsViewModel(
                    settingsRepo = app.settingsRepo!!,
                    appsRepo = app.appsRepo!!,
                    initialSettings = runBlocking { app.settingsRepo!!.fetchInitial() }
                )
            }
        }
    }

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch { action() }
    }


    fun updateSettings(
        action: suspend CoroutineScope.(LauncherSettings.Builder) -> (LauncherSettings.Builder)
    ) {
        launch { settingsRepo.update { runBlocking { action(it) } } }
    }

    fun updateSettingsFromBytes(bytes: ByteArray) {
        launch { settingsRepo.updateFromBytes(bytes) }
    }

    val settings = SettingsField(flow = settingsFlow) {
        launch { settingsRepo.update { it } }
    }

    val splitScreenSeparator = SettingsField(
        flow = settingsFlow.map { it.appCardSplitScreenSeparator }
    ) { newValue ->
        updateSettings { it.setAppCardSplitScreenSeparator(newValue) }
    }

    val appCardRemoveSpaces = SettingsField(
        flow = settingsFlow.map { it.appCardLabelRemoveSpaces }
    ) { newValue ->
        updateSettings { it.setAppCardLabelRemoveSpaces(newValue) }
    }

    fun backupReset() {
        updateSettings { LauncherSettingsSerializer.defaultValue.toBuilder() }
    }

    fun setHorizontalArrangement(
        horizontalArrangement: LauncherHorizontalArrangement
    ) {
        updateSettings {
            it.setAppLayoutHorizontalArrangement(horizontalArrangement)
        }
    }

    fun setAppCardLowercase(lowercase: Boolean) {
        updateSettings { it.setAppCardLabelLowercase(lowercase) }
    }

    fun setAppCardTextColor(textColor: LauncherTextColor) {
        updateSettings { it.setAppCardTextColor(textColor) }
    }

    fun setAppCardPadding(padding: Int) {
        updateSettings { it.setAppCardPadding(padding) }
    }

    fun setHiddenApps(hiddenPackages: List<String>) {
        launch { appsRepo.setHiddenApps(hiddenPackages) }
    }

    fun addSplitScreenShortcut(appTop: LauncherApp, appBottom: LauncherApp) {
        launch {
            appsRepo.addSplitScreenShortcut(
                appTop.packageName,
                appBottom.packageName
            )
        }
    }

    fun removeSplitScreenShortcut(shortcut: LauncherSplitScreenShortcut) {
        launch {
            appsRepo.removeSplitScreenShortcut(shortcut)
        }
    }
}
