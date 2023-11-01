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
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class LauncherViewModel(
    launcherSettingsRepository: LauncherSettingsRepository,
    private val launcherAppsRepository: LauncherAppsRepository,
    private val launcherAppsManager: LauncherAppsManager,
    val initialSettings: LauncherSettings,
) : ViewModel() {

    val initialApps: LauncherApps = LauncherApps.getDefaultInstance()
    val initialSplitScreenShortcuts: LauncherSplitScreenShortcut =
        LauncherSplitScreenShortcut.getDefaultInstance()
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

    fun launchSplitScreen(shortcut: LauncherSplitScreenShortcut) {
        launcherAppsManager.launchSplitScreen(shortcut)
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
