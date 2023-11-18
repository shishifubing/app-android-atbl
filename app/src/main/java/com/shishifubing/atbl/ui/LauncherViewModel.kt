package com.shishifubing.atbl.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.shishifubing.atbl.LauncherAppShortcut
import com.shishifubing.atbl.LauncherApplication
import com.shishifubing.atbl.LauncherApps
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LauncherViewModel(
    launcherSettingsRepository: LauncherSettingsRepository,
    private val launcherAppsRepository: LauncherAppsRepository,
    private val launcherAppsManager: LauncherAppsManager,
    val initialSettings: LauncherSettings
) : ViewModel() {

    val initialApps: LauncherApps = LauncherApps
        .getDefaultInstance()
        .toBuilder().setIsHomeApp(true).build()
    val settingsFlow = launcherSettingsRepository.settingsFlow
    private val _showHiddenApps = MutableStateFlow(false)
    val showHiddenAppsFlow = _showHiddenApps.asStateFlow()
    val appsFlow = launcherAppsRepository.appsFlow
        .combine(settingsFlow) { apps, settings ->
            apps.toBuilder().clearApps().addAllApps(apps.appsList
                .let {
                    when (settings.appLayoutSortBy) {
                        LauncherSortBy.SortByLabel -> it.sortedBy { app -> app.label }
                        else -> it.sortedBy { app -> app.label }
                    }
                }
                .let {
                    if (settings.appLayoutReverseOrder) it.reversed() else it
                }
            ).build()
        }.combine(showHiddenAppsFlow) { apps, doShow ->
            if (doShow) {
                apps
            } else {
                apps.toBuilder().clearApps()
                    .addAllApps(apps.appsList.filterNot { it.isHidden })
                    .build()
            }
        }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as LauncherApplication)
                LauncherViewModel(
                    launcherSettingsRepository = app.launcherSettingsRepo!!,
                    launcherAppsManager = app.launcherAppsManager!!,
                    launcherAppsRepository = app.launcherAppsRepo!!,
                    initialSettings = runBlocking { app.launcherSettingsRepo!!.fetchInitial() }
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
        launcherAppsRepository.getAppIcon(packageName)

    fun toggleIsHidden(packageName: String) {
        launch { launcherAppsRepository.toggleIsHidden(packageName) }
    }

    fun launchApp(packageName: String) {
        launcherAppsManager.launchApp(packageName)
    }

    fun launchSplitScreen(shortcut: LauncherSplitScreenShortcut) {
        launcherAppsManager.launchSplitScreen(shortcut)
    }

    fun launchAppInfo(packageName: String) {
        launcherAppsManager.launchAppInfo(packageName)
    }

    fun launchAppUninstall(packageName: String) {
        launcherAppsManager.launchAppUninstall(packageName)
    }

    fun deleteSplitScreenShortcut(shortcut: LauncherSplitScreenShortcut) {
        launch { launcherAppsRepository.removeSplitScreenShortcut(shortcut) }
    }

    fun launchAppShortcut(shortcut: LauncherAppShortcut) {
        launcherAppsManager.launchAppShortcut(shortcut)
    }
}
