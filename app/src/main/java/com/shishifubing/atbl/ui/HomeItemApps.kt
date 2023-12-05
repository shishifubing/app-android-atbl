package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shishifubing.atbl.Model

@Immutable
data class HomeItemAppsState(
    val apps: List<Model.App>,
    val launchApp: (Model.App) -> Unit,
    val launchShortcut: (Model.AppShortcut) -> Unit,
    val transformLabel: (String, Model.Settings.AppCard) -> String,
    val launchAppInfo: (Model.App) -> Unit,
    val launchAppUninstall: (Model.App) -> Unit,
    val setIsHidden: (Model.App, Boolean) -> Unit,
    val showShortcuts: Boolean,
    val showHiddenApps: Boolean,
    val settings: Model.Settings.AppCard
)

@Composable
fun HomeItemApps(state: HomeItemAppsState) {
    var dialogApp by remember { mutableStateOf<Model.App?>(null) }
    state.apps.forEach { app ->
        key(app.hashCode()) {
            if (state.showHiddenApps || !app.isHidden) {
                HomeRowItemCard(
                    label = state.transformLabel(app.label, state.settings),
                    onClick = { state.launchApp(app) },
                    onLongClick = { dialogApp = app },
                    settings = state.settings,
                )
            }
        }
    }
    dialogApp?.let { app ->
        HomeDialog(
            onDismissRequest = { dialogApp = null },
            actionButtons = app.shortcutsList
                .takeIf { state.showShortcuts }
                ?.map {
                    it.label to {
                        state.launchShortcut(it)
                        dialogApp = app
                    }
                }
                ?.let { HomeDialogButtons(it) }
                ?: HomeDialogButtons(listOf()),
            headers = HomeDialogHeaders(listOf {
                HomeDialogHeader(
                    app = app,
                    onDismissRequest = { dialogApp = null },
                    launchAppInfo = state.launchAppInfo,
                    launchAppUninstall = state.launchAppUninstall,
                    setIsHidden = state.setIsHidden
                )
            })
        )
    }
}
