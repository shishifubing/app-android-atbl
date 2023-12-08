package com.shishifubing.atbl.ui


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.Defaults
import com.shishifubing.atbl.LauncherManager
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.Model.Screen
import com.shishifubing.atbl.Model.Settings
import com.shishifubing.atbl.Model.Settings.AppCard
import com.shishifubing.atbl.Model.SplitScreenShortcut
import com.shishifubing.atbl.Model.SplitScreenShortcuts
import com.shishifubing.atbl.data.HomeDialogState
import com.shishifubing.atbl.data.HomeDialogState.HeaderActions
import com.shishifubing.atbl.data.HomeDialogState.LauncherDialogActions
import com.shishifubing.atbl.data.HomeDialogState.LauncherDialogState
import com.shishifubing.atbl.data.HomeState
import com.shishifubing.atbl.launcherViewModelFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val tag = HomeState::class.java.name


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

    private val _errorFlow = MutableStateFlow<Throwable?>(null)
    val errorFlow = _errorFlow.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _errorFlow.update { e }
    }

    private var prevState = Defaults.State

    private val stateFlow = stateRepo.observeState().map { stateResult ->
        stateResult.fold(
            onSuccess = {
                prevState = it
                it
            },
            onFailure = {
                _errorFlow.update { it }
                prevState
            }
        )
    }

    private val itemsFlow = stateFlow
        .map {
            it.screensList.map { screen ->
                screenToHomeRowItems(screen, it)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = listOf()
        )

    private val buttonsFlow = stateFlow
        .map { appShortcutsToDialogButtons(apps = it.apps) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = HomeDialogState.AppShortcutButtons(mapOf())
        )

    val uiState = combine(
        stateFlow,
        itemsFlow,
        buttonsFlow
    ) { state, items, buttons ->
        if (items.isEmpty()) {
            HomeState.Loading
        } else {
            HomeState.Success(
                settings = state.settings,
                showHiddenApps = state.showHiddenApps,
                isHomeApp = state.isHomeApp,
                items = items,
                appShortcutButtons = buttons
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeState.Loading
    )

    private fun appShortcutsToDialogButtons(apps: Model.Apps): HomeDialogState.AppShortcutButtons {
        return HomeDialogState.AppShortcutButtons(apps.appsMap
            .map { (packageName, app) ->
                packageName to HomeDialogState.Buttons(
                    buttons = app.shortcutsList.map { shortcut ->
                        HomeDialogState.Button(
                            label = shortcut.label,
                            id = shortcut
                        )
                    }
                )
            }
            .toMap())
    }

    private fun screenToHomeRowItems(
        screen: Screen,
        state: Model.State
    ): HomeState.RowItems {
        val items = screen.itemsList
            .flatMap { itemModel ->
                itemToHomeRowItems(
                    apps = state.apps,
                    splitScreenShortcuts = state.splitScreenShortcuts,
                    item = itemModel,
                    settings = state.settings.appCard
                )
            }
            .let { items ->
                when (val sort = state.settings.layout.sortBy) {
                    Settings.SortBy.SortByLabel -> items.sortedBy { it.label }

                    else -> throw NotImplementedError("not implemented sort - $sort")
                }
            }
            .let { items ->
                if (state.settings.layout.reverseOrder) {
                    items.reversed()
                } else {
                    items
                }
            }
        return HomeState.RowItems(items = items)
    }

    private fun itemToHomeRowItems(
        apps: Model.Apps,
        splitScreenShortcuts: SplitScreenShortcuts,
        item: Model.ScreenItem,
        settings: AppCard
    ): List<HomeState.RowItem> {
        return when (item.itemCase) {
            Model.ScreenItem.ItemCase.APP -> {
                val model = apps.getAppsOrThrow(item.app.packageName)
                listOf(
                    HomeState.RowItem.App(
                        app = model,
                        label = transformLabel(model, settings)
                    )
                )
            }

            Model.ScreenItem.ItemCase.APPS -> {
                apps.appsMap.values.map { app ->
                    HomeState.RowItem.App(
                        app = app,
                        label = transformLabel(app, settings)
                    )
                }
            }

            Model.ScreenItem.ItemCase.SHORTCUT -> {
                val model =
                    splitScreenShortcuts.getShortcutsOrThrow(item.shortcut.key)
                listOf(
                    HomeState.RowItem.SplitScreenShortcut(
                        shortcut = model,
                        label = transformLabel(model, settings)
                    )
                )
            }

            Model.ScreenItem.ItemCase.SHORTCUTS -> {
                splitScreenShortcuts.shortcutsMap.map {
                    HomeState.RowItem.SplitScreenShortcut(
                        shortcut = it.value,
                        label = transformLabel(it.value, settings)
                    )
                }
            }

            Model.ScreenItem.ItemCase.ITEM_NOT_SET -> {
                Log.d(tag, "item is not set for some reason?")
                listOf()
            }

            else -> {
                throw NotImplementedError("not implemented item: $item")
            }
        }
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

    fun onHeaderAction(app: Model.App, action: HeaderActions) {
        when (action) {
            HeaderActions.GoToInfo -> managerAction {
                this.launchAppInfo(app.packageName)
            }

            HeaderActions.HideOrShow -> stateAction {
                setIsHidden(app.packageName, !app.isHidden)
            }

            HeaderActions.Uninstall -> managerAction {
                this.launchAppUninstall(app.packageName)
            }
        }
    }

    fun onLauncherDialogAction(
        state: LauncherDialogState,
        action: LauncherDialogActions,
        navigate: (LauncherNav) -> Unit,
    ) {
        when (action) {
            LauncherDialogActions.AddScreenAfter -> {
                stateAction { this.addScreenAfter(state.currentPage) }
            }

            LauncherDialogActions.AddScreenBefore -> {
                stateAction { this.addScreenBefore(state.currentPage) }
            }

            LauncherDialogActions.GoToAddWidget -> navigate(LauncherNav.AddWidget)
            LauncherDialogActions.GoToSettings -> navigate(LauncherNav.Settings)
            LauncherDialogActions.RemoveScreen -> stateAction {
                this.removeScreen(state.currentPage)
            }

            LauncherDialogActions.ShowHiddenApps -> {
                stateAction { this.setShowHiddenApps(true) }
            }

            LauncherDialogActions.HideHiddenApps -> {
                stateAction { this.setShowHiddenApps(false) }
            }
        }
    }

    fun onRowItemClick(item: HomeState.RowItem) {
        when (item) {
            is HomeState.RowItem.App -> {
                managerAction { this.launchApp(item.app.packageName) }
            }

            is HomeState.RowItem.SplitScreenShortcut -> managerAction {
                launchSplitScreen(
                    item.shortcut.appFirst.packageName,
                    item.shortcut.appSecond.packageName
                )
            }
        }
    }

    fun removeSplitScreenShortcut(shortcut: SplitScreenShortcut) {
        stateAction { removeSplitScreenShortcut(shortcut.key) }
    }

    fun launchShortcut(shortcut: Model.AppShortcut) {
        managerAction { this.launchAppShortcut(shortcut) }
    }

    private fun managerAction(action: suspend LauncherManager.() -> Unit) {
        launch { action.invoke(manager) }
    }

    private fun stateAction(action: suspend LauncherStateRepository.() -> Unit) {
        launch { action.invoke(stateRepo) }
    }

    private fun launch(action: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) { action() }
    }
}



