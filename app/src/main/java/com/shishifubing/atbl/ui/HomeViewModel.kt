package com.shishifubing.atbl.ui


import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.LauncherManager
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.Model.Settings.AppCard
import com.shishifubing.atbl.Model.SplitScreenShortcut
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

@Immutable
sealed interface HomeState {
    @Immutable
    data class Success(
        val screens: List<ScreenState>,
    ) : HomeState

    @Immutable
    data object Loading : HomeState

    @Immutable
    data class ScreenState(
        val items: List<ItemState>,
        val isHomeApp: Boolean,
        val settings: Model.Settings
    )

    @Immutable
    sealed interface ItemState {
        @Immutable
        data class Apps(val state: HomeItemAppsState) : ItemState

        @Immutable
        data class Shortcuts(
            val state: HomeItemSplitScreenShortcutsState
        ) : ItemState
    }
}

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
        HomeState.Success(
            screens = state.screensList.map { screen ->
                HomeState.ScreenState(
                    isHomeApp = state.isHomeApp,
                    items = getHomeItems(state, screen, showHiddenApps),
                    settings = state.settings
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeState.Loading
    )

    private fun launchSplitScreenShortcut(shortcut: SplitScreenShortcut) {
        manager.launchSplitScreen(
            shortcut.appFirst.packageName,
            shortcut.appSecond.packageName
        )
    }

    private fun removeSplitScreenShortcut(shortcut: SplitScreenShortcut) {
        stateAction { removeSplitScreenShortcut(shortcut) }
    }

    private fun launchAppUninstall(app: Model.App) {
        manager.launchAppUninstall(app.packageName)
    }

    private fun setIsHidden(app: Model.App, isHidden: Boolean) {
        stateAction { setIsHidden(app.packageName, isHidden) }
    }

    private fun launchAppInfo(app: Model.App) {
        manager.launchAppInfo(app.packageName)
    }

    private fun launchApp(app: Model.App) {
        manager.launchApp(app.packageName)
    }

    private fun launchShortcut(shortcut: Model.AppShortcut) {
        manager.launchAppShortcut(shortcut)
    }

    private fun transformLabel(
        label: String,
        settings: AppCard
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

    private fun stateAction(action: suspend LauncherStateRepository.() -> Unit) {
        launch { action.invoke(stateRepo) }
    }

    private fun getSplitScreenShortcutLabel(
        shortcut: SplitScreenShortcut,
        settings: AppCard
    ): String {
        return transformLabel(
            shortcut.appSecond.label
                    + settings.splitScreenSeparator
                    + shortcut.appFirst.label,
            settings
        )
    }

    private fun getHomeItems(
        state: Model.State,
        screen: Model.Screen,
        showHiddenApps: Boolean
    ): List<HomeState.ItemState> {
        return screen.itemsList.mapNotNull { item ->
            if (item.hasComplex()) {
                when (item.complex) {
                    Model.ItemComplex.APPS ->
                        HomeState.ItemState.Apps(
                            getHomeItemAppsState(
                                apps = state.apps,
                                isHomeApp = state.isHomeApp,
                                settings = state.settings,
                                showHiddenApps = showHiddenApps
                            )
                        )

                    Model.ItemComplex.SPLIT_SCREEN_SHORTCUTS ->
                        HomeState.ItemState.Shortcuts(
                            getHomeItemSplitScreenShortcutsState(
                                shortcuts = state.splitScreenShortcuts,
                                settings = state.settings.appCard
                            )
                        )

                    else -> null
                }
            } else {
                null
            }
        }
    }

    private fun getHomeItemSplitScreenShortcutsState(
        shortcuts: Model.SplitScreenShortcuts,
        settings: AppCard
    ): HomeItemSplitScreenShortcutsState {
        return HomeItemSplitScreenShortcutsState(
            shortcuts = shortcuts.shortcutsMap.values.sortedBy { it.key },
            getLabel = ::getSplitScreenShortcutLabel,
            launchSplitScreenShortcut = ::launchSplitScreenShortcut,
            removeSplitScreenShortcut = ::removeSplitScreenShortcut,
            launchAppInfo = ::launchAppInfo,
            launchAppUninstall = ::launchAppUninstall,
            setIsHidden = ::setIsHidden,
            settings = settings
        )
    }

    private fun getHomeItemAppsState(
        apps: Model.Apps,
        isHomeApp: Boolean,
        settings: Model.Settings,
        showHiddenApps: Boolean,
    ): HomeItemAppsState {
        val appList = apps.appsMap.values
            .let {
                when (settings.layout.sortBy) {
                    Model.Settings.SortBy.SortByLabel ->
                        it.sortedBy { app -> app.label }

                    else -> it.sortedBy { app -> app.label }
                }
            }
            .let {
                if (settings.layout.reverseOrder) it.reversed() else it
            }
            .toList()
        return HomeItemAppsState(
            apps = appList,
            launchApp = ::launchApp,
            launchShortcut = ::launchShortcut,
            transformLabel = ::transformLabel,
            launchAppInfo = ::launchAppInfo,
            launchAppUninstall = ::launchAppUninstall,
            setIsHidden = ::setIsHidden,
            showShortcuts = isHomeApp,
            showHiddenApps = showHiddenApps,
            settings = settings.appCard
        )
    }
}



