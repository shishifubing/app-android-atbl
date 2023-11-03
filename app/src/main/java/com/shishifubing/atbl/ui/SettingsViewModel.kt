package com.shishifubing.atbl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.LauncherApps
import com.shishifubing.atbl.LauncherAppsRepository
import com.shishifubing.atbl.LauncherSettings
import com.shishifubing.atbl.LauncherSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class SettingsViewModel(
    private val launcherSettingsRepository: LauncherSettingsRepository,
    private val appsRepository: LauncherAppsRepository,
    val initialSettings: LauncherSettings,
    val initialApps: LauncherApps = LauncherApps.getDefaultInstance()
) : ViewModel() {

    val settingsFlow = launcherSettingsRepository.settingsFlow
    val appsFlow = appsRepository.appsFlow

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch { action() }
    }

    fun reloadApps() = launch { appsRepository.reloadApps() }

    fun updateSettings(action: (LauncherSettings.Builder) -> (LauncherSettings.Builder)) {
        launch { launcherSettingsRepository.update(action) }
    }

    fun setHiddenApps(hiddenPackages: List<String>) {
        launch { appsRepository.setHiddenApps(hiddenPackages) }
    }

}

class SettingsViewModelFactory(
    private val repository: LauncherSettingsRepository,
    private val appsRepository: LauncherAppsRepository,
    private val initialSettings: LauncherSettings,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                repository, appsRepository, initialSettings
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
