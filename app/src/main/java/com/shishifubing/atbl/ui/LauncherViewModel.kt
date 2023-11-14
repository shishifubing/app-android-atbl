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
    val appsFlow = launcherAppsRepository.appsFlow.combine(
        settingsFlow,
        this::transformApps
    )

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

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch { action() }
    }

    private fun transformApps(
        current: LauncherApps,
        settings: LauncherSettings
    ): LauncherApps {
        return current.toBuilder().clearApps().addAllApps(current.appsList
            .filterNot { it.isHidden }
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
    }

    fun getAppIcon(packageName: String) =
        launcherAppsRepository.getAppIcon(packageName)

    fun hideApp(packageName: String) {
        launch { launcherAppsRepository.hideApp(packageName) }
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
