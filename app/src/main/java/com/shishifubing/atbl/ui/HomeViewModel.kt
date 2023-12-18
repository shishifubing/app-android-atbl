package com.shishifubing.atbl.ui


import com.shishifubing.atbl.LauncherManager
import com.shishifubing.atbl.LauncherNavigator
import com.shishifubing.atbl.LauncherStateRepository
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.Model.Settings.AppCard
import com.shishifubing.atbl.data.HomeDialogState.HeaderActions
import com.shishifubing.atbl.data.HomeDialogState.LauncherDialogAction
import com.shishifubing.atbl.launcherViewModelFactory

class HomeViewModel(
    private val stateRepo: LauncherStateRepository,
    private val manager: LauncherManager,
    private val navigator: LauncherNavigator
) : BaseViewModel(stateRepo, navigator) {
    companion object {
        val Factory = launcherViewModelFactory {
            HomeViewModel(
                manager = manager,
                stateRepo = stateRepo,
                navigator = navigator
            )
        }
    }

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
            LauncherDialogAction.GoToSettings -> navigator.goToRoute(
                Routes.settings
            )

            LauncherDialogAction.ShowHiddenApps -> stateAction {
                setShowHiddenApps(true)
            }

            LauncherDialogAction.HideHiddenApps -> stateAction {
                setShowHiddenApps(false)
            }
        }
    }

    fun onRowItemClick(item: Model.App) {
        managerAction { launchApp(item.packageName) }
    }

    fun onAppDialogClick(shortcut: Model.AppShortcut) {
        managerAction { launchAppShortcut(shortcut) }
    }

    private fun managerAction(action: suspend LauncherManager.() -> Unit) {
        launch { action.invoke(manager) }
    }

    fun sortRowItems(
        items: Model.Apps,
        settings: Model.Settings.Layout
    ): List<Model.App> {
        val res = items.appsMap.values.toMutableList()
        when (settings.sortBy) {
            Model.Settings.SortBy.SortByLabel, Model.Settings.SortBy.UNRECOGNIZED, null -> {
                res.sortBy { it.label }
            }
        }
        if (settings.reverseOrder) {
            res.reverse()
        }
        return res
    }

    fun getRowItemLabel(
        app: Model.App,
        settings: AppCard
    ): String {
        return app.label
            .let {
                if (settings.labelRemoveSpaces) {
                    it.replace(" ", "")
                } else {
                    it
                }
            }
            .let { if (settings.labelLowercase) it.lowercase() else it }
    }
}



