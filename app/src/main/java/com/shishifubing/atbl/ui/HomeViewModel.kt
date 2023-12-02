package com.shishifubing.atbl.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.LauncherManager
import com.shishifubing.atbl.LauncherScreenItemComplex
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.data.UIApp
import com.shishifubing.atbl.data.UIApps
import com.shishifubing.atbl.data.UIHomeState
import com.shishifubing.atbl.data.UISettings
import com.shishifubing.atbl.data.UiHomeStateScreen
import com.shishifubing.atbl.launcherViewModelFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val stateRepo: LauncherStateRepository,
    private val manager: LauncherManager
) : ViewModel() {
    companion object {
        val Factory = launcherViewModelFactory {
            HomeViewModel(
                manager = manager,
                stateRepo = stateRepo
            )
        }
    }

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) { action() }
    }

    private val _showHiddenAppsFlow = MutableStateFlow(false)
    val showHiddenApps = _showHiddenAppsFlow.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error = _error.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _error.update { e }
    }

    val uiState = combine(
        stateRepo.observeState(),
        _showHiddenAppsFlow
    ) { state, showHiddenApps ->
        val settings = state.settings
        val apps = transformApps(
            state.apps.appsMap.values, showHiddenApps, settings.layout
        )
        val splitScreenShortcuts = state.splitScreenShortcuts
            .shortcutsMap
            .values
            .sortedBy { it.appTop.packageName }
        val screens = state.screensList.map { screen ->
            UiHomeStateScreen(
                showHiddenApps = showHiddenApps,
                settings = UISettings(settings),
                launcherRowSettings = settings.rowSettings(),
                isHomeApp = state.isHomeApp,
                items = screen.itemsList.mapNotNull {
                    when (it.complex) {
                        LauncherScreenItemComplex.APPS ->
                            LauncherUiItem.Apps(apps)

                        LauncherScreenItemComplex.SPLIT_SCREEN_SHORTCUTS
                        -> LauncherUiItem.Shortcuts(splitScreenShortcuts)

                        else -> null
                    }
                },
            )
        }
        UIHomeState.Success(screens = screens)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UIHomeState.Loading
    )

    fun launchSplitScreenShortcut(shortcut: Model.SplitScreenShortcut) {
        manager.launchSplitScreen(shortcut)
    }

    fun removeSplitScreenShortcut(shortcut: Model.SplitScreenShortcut) {
        stateAction { removeSplitScreenShortcut(shortcut) }
    }

    fun launchAppUninstall(packageName: String) {
        manager.launchAppUninstall(packageName)
    }

    fun setIsHidden(packageName: String, isHidden: Boolean) {
        stateAction { setIsHidden(packageName, isHidden) }
    }

    fun launchAppInfo(packageName: String) {
        manager.launchAppInfo(packageName)
    }

    fun launchApp(packageName: String) {
        manager.launchApp(packageName)
    }

    fun launchShortcut(shortcut: Model.AppShortcut) {
        manager.launchAppShortcut(shortcut)
    }

    fun transformLabel(
        label: String,
        settings: Model.Settings.AppCard
    ): String {
        return label
            .let {
                if (settings.labelRemoveSpaces) {
                    it.replace(" ", "")
                } else {
                    it
                }
            }
            .let { if (settings.labelLowercase) it.lowercase() else it }
    }

    fun setShowHiddenApps(showHiddenApps: Boolean) {
        _showHiddenAppsFlow.value = showHiddenApps
    }

    fun addScreenAfter(screen: Int) {
        stateAction { addScreenAfter(screen) }
    }

    fun addScreenBefore(screen: Int) {
        stateAction { addScreenBefore(screen) }
    }

    fun removeScreen(screen: Int) {
        stateAction { removeScreen(screen) }
    }

    fun stateAction(action: suspend LauncherStateRepository.() -> Unit) {
        launch { action.invoke(stateRepo) }
    }

    private fun transformApps(
        apps: Collection<Model.App>,
        showHiddenApps: Boolean,
        settings: Model.Settings.Layout,
    ): UIApps {
        return apps
            .let {
                when (settings.sortBy) {
                    Model.Settings.SortBy.SortByLabel -> it.sortedBy { app
                        ->
                        app.label
                    }

                    else -> it.sortedBy { app -> app.label }
                }
            }
            .let {
                if (settings.reverseOrder) it.reversed() else it
            }
            .let {
                if (showHiddenApps) it else it.filterNot { app -> app.isHidden }
            }
            .map { UIApp(it) }
            .let { UIApps(it) }
    }
}


