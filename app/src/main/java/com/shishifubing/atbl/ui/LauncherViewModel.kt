package com.shishifubing.atbl.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.LauncherAppShortcut
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

class LauncherViewModel(
    launcherSettingsRepository: LauncherSettingsRepository,
    private val launcherAppsRepository: LauncherAppsRepository,
    private val launcherAppsManager: LauncherAppsManager,
    val initialSettings: LauncherSettings,
) : ViewModel() {

    val initialApps: LauncherApps = LauncherApps
        .getDefaultInstance()
        .toBuilder().setIsHomeApp(true).build()
    val settingsFlow = launcherSettingsRepository.settingsFlow
    val appsFlow = launcherAppsRepository.appsFlow.combine(
        settingsFlow,
        this::transformApps
    )

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

class LauncherViewModelFactory(
    private val launcherSettingsRepository: LauncherSettingsRepository,
    private val launcherAppsRepository: LauncherAppsRepository,
    private val launcherAppsManager: LauncherAppsManager,
    private val initialSettings: LauncherSettings
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LauncherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LauncherViewModel(
                launcherSettingsRepository,
                launcherAppsRepository,
                launcherAppsManager,
                initialSettings
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
