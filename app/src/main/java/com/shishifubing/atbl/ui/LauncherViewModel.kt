package com.shishifubing.atbl.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.LauncherAppShortcut
import com.shishifubing.atbl.LauncherApps
import com.shishifubing.atbl.LauncherAppsManager
import com.shishifubing.atbl.LauncherAppsRepository
import com.shishifubing.atbl.LauncherSettings
import com.shishifubing.atbl.LauncherSettingsRepository
import com.shishifubing.atbl.LauncherSortBy
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class LauncherViewModel(
    launcherSettingsRepository: LauncherSettingsRepository,
    private val launcherAppsRepository: LauncherAppsRepository,
    private val launcherAppsManager: LauncherAppsManager,
    val initialSettings: LauncherSettings,
    initialApps: LauncherApps
) : ViewModel() {

    val initialApps = transformApps(initialApps, initialSettings)

    val settingsFlow = launcherSettingsRepository.settingsFlow
    val appsFlow = launcherAppsRepository.appsFlow.combine(
        settingsFlow,
        this::transformApps
    )

    private fun transformApps(
        current: LauncherApps,
        settings: LauncherSettings
    ): LauncherApps {
        return LauncherApps.newBuilder().addAllApps(
            current.appsList
                .filterNot { it.isHidden }
                .let {
                    when (settings.appLayoutSortBy) {
                        LauncherSortBy.SortByLabel -> it.sortedBy { app -> app.label }
                        else -> it.sortedBy { app -> app.label }
                    }
                }
                .let { if (settings.appLayoutReverseOrder) it.reversed() else it }
        ).build()
    }

    fun getAppIcon(packageName: String) =
        launcherAppsRepository.getAppIcon(packageName)

    fun hideApp(packageName: String) {
        viewModelScope.launch { launcherAppsRepository.hideApp(packageName) }
    }

    fun launchApp(packageName: String) {
        launcherAppsManager.launchApp(packageName)
    }

    fun launchSplitScreen(app1: String, app2: String) {
        launcherAppsManager.launchSplitScreen(app1, app2)
    }

    fun launchAppInfo(packageName: String) {
        launcherAppsManager.launchAppInfo(packageName)
    }

    fun launchAppUninstall(packageName: String) {
        launcherAppsManager.launchAppUninstall(packageName)
    }

    fun launchAppShortcut(shortcut: LauncherAppShortcut) {
        launcherAppsManager.launchAppShortcut(shortcut)
    }
}

class LauncherViewModelFactory(
    private val launcherSettingsRepository: LauncherSettingsRepository,
    private val launcherAppsRepository: LauncherAppsRepository,
    private val launcherAppsManager: LauncherAppsManager,
    private val initialSettings: LauncherSettings,
    private val initialApps: LauncherApps
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LauncherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LauncherViewModel(
                launcherSettingsRepository,
                launcherAppsRepository,
                launcherAppsManager,
                initialSettings,
                initialApps
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
