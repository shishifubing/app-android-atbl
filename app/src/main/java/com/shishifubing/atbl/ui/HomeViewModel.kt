package com.shishifubing.atbl.ui


import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.LauncherManager
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.Model.Screen
import com.shishifubing.atbl.Model.Settings
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

private val tag = HomeState::class.java.name

@Immutable
sealed interface HomeState {
    @Immutable
    data class Success(
        val items: List<RowItems>,
        val settings: Settings,
        val showHiddenApps: Boolean,
        val isHomeApp: Boolean,
        val appShortcutButtons: Map<String, HomeDialogButtons>
    ) : HomeState

    @Immutable
    data object Loading : HomeState

    @Immutable
    data class RowItems(val items: List<RowItem>)

    @Immutable
    sealed interface RowItem {
        val label: String

        @Immutable
        data class App(
            val app: Model.App,
            override val label: String
        ) : RowItem

        @Immutable
        data class SplitScreenShortcut(
            val shortcut: Model.SplitScreenShortcut,
            override val label: String
        ) : RowItem
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
            settings = state.settings,
            showHiddenApps = showHiddenApps,
            isHomeApp = state.isHomeApp,
            items = state.screensList.map { screen ->
                screenToHomeRowItems(screen, state)
            },
            appShortcutButtons = appShortcutsToDialogButtons(apps = state.apps)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeState.Loading
    )

    private fun appShortcutsToDialogButtons(apps: Model.Apps): Map<String, HomeDialogButtons> {
        return apps.appsMap
            .map { (packageName, app) ->
                packageName to HomeDialogButtons(
                    model = app.shortcutsList.map { shortcut ->
                        shortcut.label to { launchShortcut(shortcut) }
                    }
                )
            }
            .toMap()
    }

    private fun screenToHomeRowItems(
        screen: Screen,
        state: Model.State
    ): HomeState.RowItems {
        val items = mutableListOf<HomeState.RowItem>()
        screen.itemsList.forEach { item ->
            when (item.itemCase) {
                Model.ScreenItem.ItemCase.APP -> {
                    val model = state.apps.getAppsOrThrow(item.app.packageName)
                    val app = HomeState.RowItem.App(
                        app = model,
                        label = transformLabel(model, state.settings.appCard)
                    )
                    items.add(app)
                }

                Model.ScreenItem.ItemCase.APPS -> {
                    val apps = state.apps.appsMap.values.map { app ->
                        HomeState.RowItem.App(
                            app = app,
                            label = transformLabel(app, state.settings.appCard)
                        )
                    }
                    items.addAll(apps)
                }

                Model.ScreenItem.ItemCase.SHORTCUT -> {
                    val model = state.splitScreenShortcuts
                        .getShortcutsOrThrow(item.shortcut.key)
                    val shortcut = HomeState.RowItem.SplitScreenShortcut(
                        shortcut = model,
                        label = transformLabel(model, state.settings.appCard)
                    )
                    items.add(shortcut)
                }

                Model.ScreenItem.ItemCase.SHORTCUTS -> {
                    val shortcuts =
                        state.splitScreenShortcuts.shortcutsMap.map {
                            HomeState.RowItem.SplitScreenShortcut(
                                shortcut = it.value,
                                label = transformLabel(
                                    it.value,
                                    state.settings.appCard
                                )
                            )
                        }
                    items.addAll(shortcuts)
                }

                Model.ScreenItem.ItemCase.ITEM_NOT_SET -> {
                    throw IllegalArgumentException("item is not set")
                }

                else -> {
                    throw NotImplementedError("not implemented item: $item")
                }
            }
        }
        return transformItems(items, state.settings.layout)
    }

    private fun transformItems(
        items: MutableList<HomeState.RowItem>,
        settings: Settings.Layout
    ): HomeState.RowItems {
        when (settings.sortBy) {
            Settings.SortBy.SortByLabel -> items.sortBy { it.label }

            else -> throw NotImplementedError("not implemented sort - ${settings.sortBy}")
        }
        if (settings.reverseOrder) {
            items.reverse()
        }
        return HomeState.RowItems(items = items)
    }

    private fun transformLabel(app: Model.App, settings: AppCard): String {
        return transformLabel(app.label, settings)
    }

    private fun transformLabel(
        shortcut: SplitScreenShortcut,
        settings: AppCard
    ): String {
        return transformLabel(
            shortcut.appSecond.label + settings.splitScreenSeparator + shortcut.appFirst.label,
            settings
        )
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

    fun launchSplitScreenShortcut(shortcut: SplitScreenShortcut) {
        manager.launchSplitScreen(
            shortcut.appFirst.packageName,
            shortcut.appSecond.packageName
        )
    }

    fun removeSplitScreenShortcut(shortcut: SplitScreenShortcut) {
        stateAction { removeSplitScreenShortcut(shortcut.key) }
    }

    fun launchAppUninstall(app: Model.App) {
        manager.launchAppUninstall(app.packageName)
    }

    fun setIsHidden(app: Model.App, isHidden: Boolean) {
        stateAction { setIsHidden(app.packageName, isHidden) }
    }

    fun launchAppInfo(app: Model.App) {
        manager.launchAppInfo(app.packageName)
    }

    fun launchApp(app: Model.App) {
        manager.launchApp(app.packageName)
    }

    fun launchShortcut(shortcut: Model.AppShortcut) {
        manager.launchAppShortcut(shortcut)
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
}



