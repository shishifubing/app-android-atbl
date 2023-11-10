package com.shishifubing.atbl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherApps
import com.shishifubing.atbl.LauncherSettings
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.domain.LauncherAppsRepository
import com.shishifubing.atbl.domain.LauncherSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class SettingsViewModel(
    private val launcherSettingsRepository: LauncherSettingsRepository,
    private val launcherAppsRepository: LauncherAppsRepository,
    val initialSettings: LauncherSettings,
    val closeSettings: () -> Unit,
    val initialApps: LauncherApps = LauncherApps.getDefaultInstance(),
) : ViewModel() {

    val settingsFlow = launcherSettingsRepository.settingsFlow
    val appsFlow = launcherAppsRepository.appsFlow

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch { action() }
    }

    fun reloadApps() = launch { launcherAppsRepository.reloadApps() }

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

class SettingsViewModelFactory(
    private val repository: LauncherSettingsRepository,
    private val appsRepository: LauncherAppsRepository,
    private val initialSettings: LauncherSettings,
    private val closeSettings: () -> Unit
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                repository, appsRepository, initialSettings, closeSettings
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
