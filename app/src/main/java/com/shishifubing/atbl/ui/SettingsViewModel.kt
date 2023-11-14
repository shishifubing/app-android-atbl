package com.shishifubing.atbl.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherApplication
import com.shishifubing.atbl.LauncherApps
import com.shishifubing.atbl.LauncherSettings
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.domain.LauncherAppsRepository
import com.shishifubing.atbl.domain.LauncherSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingsViewModel(
    private val launcherSettingsRepository: LauncherSettingsRepository,
    private val launcherAppsRepository: LauncherAppsRepository,
    val initialSettings: LauncherSettings
) : ViewModel() {

    val initialApps: LauncherApps = LauncherApps.getDefaultInstance()
    val settingsFlow = launcherSettingsRepository.settingsFlow
    val appsFlow = launcherAppsRepository.appsFlow

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as LauncherApplication)
                SettingsViewModel(
                    launcherSettingsRepository = app.launcherSettingsRepo!!,
                    launcherAppsRepository = app.launcherAppsRepo!!,
                    initialSettings = runBlocking { app.launcherSettingsRepo!!.fetchInitial() }
                )
            }
        }
    }

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch { action() }
    }

    fun updateSettings(action: (LauncherSettings.Builder) -> (LauncherSettings.Builder)) {
        launch { launcherSettingsRepository.update(action) }
    }

    fun setHiddenApps(hiddenPackages: List<String>) {
        launch { launcherAppsRepository.setHiddenApps(hiddenPackages) }
    }

    fun addSplitScreenShortcut(appTop: LauncherApp, appBottom: LauncherApp) {
        launch {
            launcherAppsRepository.addSplitScreenShortcut(
                appTop.packageName,
                appBottom.packageName
            )
        }
    }

    fun removeSplitScreenShortcut(shortcut: LauncherSplitScreenShortcut) {
        launch {
            launcherAppsRepository.removeSplitScreenShortcut(shortcut)
        }
    }
}
