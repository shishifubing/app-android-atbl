package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shishifubing.atbl.LauncherApp

@Composable
fun HomeItemApps(
    apps: List<LauncherApp>,
    appActions: AppActions,
    showShortcuts: Boolean,
    appCardSettings: LauncherAppCardSettings
) {
    var dialogApp by remember(apps) { mutableStateOf<LauncherApp?>(null) }
    apps.forEach { app ->
        key(app.hashCode()) {
            HomeItemCard(
                label = app.label,
                onClick = { appActions.launchApp(app.packageName) },
                onLongClick = { dialogApp = app },
                settings = appCardSettings,
                actions = appActions
            )
        }
    }
    dialogApp?.let { app ->
        HomeDialog(
            onDismissRequest = { dialogApp = null },
            actionButtons = app.shortcutsList.takeIf { showShortcuts }?.map {
                it.label to { appActions.launchShortcut(it); dialogApp = app }
            }
        ) {
            HomeDialogHeader(
                packageName = app.packageName,
                label = app.label,
                isHidden = app.isHidden,
                actions = appActions,
                onDismissRequest = { dialogApp = null }
            )
        }
    }
}
