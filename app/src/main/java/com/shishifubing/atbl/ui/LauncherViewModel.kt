package com.shishifubing.atbl.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.shishifubing.atbl.LauncherAppShortcut
import com.shishifubing.atbl.LauncherApplication
import com.shishifubing.atbl.LauncherSettings
import com.shishifubing.atbl.LauncherSortBy
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.domain.LauncherAppsManager
import com.shishifubing.atbl.domain.LauncherAppsRepository
import com.shishifubing.atbl.domain.LauncherSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LauncherViewModel(
    launcherSettingsRepository: LauncherSettingsRepository,
    private val appsRepo: LauncherAppsRepository,
    private val appsManager: LauncherAppsManager,
    val initialSettings: LauncherSettings
) : ViewModel() {
    val settingsFlow = launcherSettingsRepository.settingsFlow
    private val _showHiddenApps = MutableStateFlow(false)
    val showHiddenAppsFlow = _showHiddenApps.asStateFlow()
    val isHomeAppFlow = appsRepo.appsFlow.map { it.isHomeApp }
    val shortcutsFlow = appsRepo.appsFlow.map { it.splitScreenShortcutsList }
    val appsFlow = appsRepo.appsFlow
        .combine(settingsFlow) { apps, settings ->
            apps.appsMap.values
                .let {
                    when (settings.appLayoutSortBy) {
                        LauncherSortBy.SortByLabel -> it.sortedBy { app -> app.label }
                        else -> it.sortedBy { app -> app.label }
                    }
                }
                .let {
                    if (settings.appLayoutReverseOrder) it.reversed() else it
                }
        }.combine(showHiddenAppsFlow) { apps, doShow ->
            if (doShow) {
                apps
            } else {
                apps.filterNot { it.isHidden }
            }
        }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as LauncherApplication
                LauncherViewModel(
                    launcherSettingsRepository = app.settingsRepo!!,
                    appsManager = app.appsManager!!,
                    appsRepo = app.appsRepo!!,
                    initialSettings = runBlocking { app.settingsRepo!!.fetchInitial() }
                )
            }
        }
    }

    fun showHiddenAppsToggle() {
        _showHiddenApps.value = _showHiddenApps.value.not()
    }

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch { action() }
    }

    fun getAppIcon(packageName: String) =
        appsRepo.getAppIcon(packageName)

    fun toggleIsHidden(packageName: String) {
        launch { appsRepo.toggleIsHidden(packageName) }
    }

    fun launchApp(packageName: String) {
        appsManager.launchApp(packageName)
    }

    fun launchSplitScreen(shortcut: LauncherSplitScreenShortcut) {
        appsManager.launchSplitScreen(shortcut)
    }

    fun launchAppInfo(packageName: String) {
        appsManager.launchAppInfo(packageName)
    }

    fun launchAppUninstall(packageName: String) {
        appsManager.launchAppUninstall(packageName)
    }

    fun deleteSplitScreenShortcut(shortcut: LauncherSplitScreenShortcut) {
        launch { appsRepo.removeSplitScreenShortcut(shortcut) }
    }

    fun launchAppShortcut(shortcut: LauncherAppShortcut) {
        appsManager.launchAppShortcut(shortcut)
    }
}
