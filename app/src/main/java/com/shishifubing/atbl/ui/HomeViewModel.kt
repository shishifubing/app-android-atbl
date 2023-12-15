package com.shishifubing.atbl.ui


import androidx.lifecycle.viewModelScope
import com.shishifubing.atbl.LauncherManager
import com.shishifubing.atbl.LauncherNavigator
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.Model.Settings.AppCard
import com.shishifubing.atbl.Model.SplitScreenShortcut
import com.shishifubing.atbl.data.HomeDialogState.HeaderActions
import com.shishifubing.atbl.data.HomeDialogState.LauncherDialogAction
import com.shishifubing.atbl.data.HomeState
import com.shishifubing.atbl.data.RepoState
import com.shishifubing.atbl.data.UiState
import com.shishifubing.atbl.launcherViewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val tag = HomeState::class.java.name


class HomeViewModel(
    private val stateRepo: LauncherStateRepository,
    private val manager: LauncherManager,
    private val navigator: LauncherNavigator
) : BaseViewModel<HomeState>(stateRepo, navigator) {
    companion object {
        val Factory = launcherViewModelFactory {
            HomeViewModel(
                manager = manager,
                stateRepo = stateRepo,
                navigator = navigator
            )
        }
    }

    override val uiStateFlow = stateFlow.map { state ->
        if (state == RepoState.Loading) {
            object : UiState.Loading<HomeState> {}
        } else {
            val stateSuccess = state as RepoState.Success
            UiState.Success(
                HomeState(
                    state = stateSuccess.state,
                    items = stateToHomeRowItems(stateSuccess.state)
                )
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = object : UiState.Loading<HomeState> {}
    )

    fun onHeaderAction(app: Model.App, action: HeaderActions) {
        when (action) {
            HeaderActions.GoToInfo -> managerAction {
                launchAppInfo(app.packageName)
            }

            HeaderActions.HideOrShow -> stateAction {
                setIsHidden(app.packageName, !app.isHidden)
            }

            HeaderActions.Uninstall -> managerAction {
                launchAppUninstall(app.packageName)
            }
        }
    }

    fun onLauncherDialogAction(action: LauncherDialogAction) {
        when (action) {
            LauncherDialogAction.GoToAddWidget -> navigator.goToRoute(
                Routes.addWidget
            )

            LauncherDialogAction.GoToSettings -> navigator.goToRoute(
                Routes.settings
            )

            LauncherDialogAction.ShowHiddenApps -> stateAction {
                setShowHiddenApps(true)
            }

            LauncherDialogAction.HideHiddenApps -> stateAction {
                setShowHiddenApps(false)
            }

            LauncherDialogAction.GoToEditSplitScreenShortcuts -> navigator.goToRoute(
                Routes.shortcuts
            )
        }
    }

    fun onRowItemClick(item: HomeState.RowItem) {
        when (item) {
            is HomeState.RowItem.App -> managerAction {
                launchApp(item.app.packageName)
            }

            is HomeState.RowItem.SplitScreenShortcut -> managerAction {
                launchSplitScreen(
                    item.shortcut.appFirst.packageName,
                    item.shortcut.appSecond.packageName
                )
            }
        }
    }

    fun onSplitScreenShortcutsDialogClick(shortcut: SplitScreenShortcut) {
        stateAction { removeSplitScreenShortcut(shortcut.key) }
    }

    fun onAppDialogClick(shortcut: Model.AppShortcut) {
        managerAction { launchAppShortcut(shortcut) }
    }

    private fun managerAction(action: suspend LauncherManager.() -> Unit) {
        launch { action.invoke(manager) }
    }

    private fun stateToHomeRowItems(state: Model.State): HomeState.RowItems {
        val items = mutableListOf<HomeState.RowItem>()
        val shortcuts = state.splitScreenShortcuts.shortcutsMap.map {
            HomeState.RowItem.SplitScreenShortcut(
                shortcut = it.value,
                label = transformLabel(it.value, state.settings.appCard)
            )
        }
        val apps = state.apps.appsMap.map {
            HomeState.RowItem.App(
                app = it.value,
                label = transformLabel(it.value, state.settings.appCard)
            )
        }
        items.addAll(shortcuts)
        items.addAll(apps)

        when (state.settings.layout.sortBy) {
            Model.Settings.SortBy.SortByLabel, Model.Settings.SortBy.UNRECOGNIZED, null -> {
                items.sortBy { it.label }
            }
        }
        if (state.settings.layout.reverseOrder) {
            items.reverse()
        }
        return HomeState.RowItems(items = items)
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
}



