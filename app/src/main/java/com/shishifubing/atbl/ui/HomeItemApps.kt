package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherAppShortcut

@Composable
fun HomeItemApps(
    apps: List<LauncherApp>,
    actions: AppActions,
    showShortcuts: Boolean,
    launchShortcut: (LauncherAppShortcut) -> Unit,
    appCardSettings: LauncherAppCardSettings
) {
    var dialogAppIndex by remember(apps) { mutableIntStateOf(-1) }
    apps.forEachIndexed { i, app ->
        HomeAppCard(
            label = app.label,
            onClick = { actions.launchApp(app.packageName) },
            onLongClick = { dialogAppIndex = i },
            settings = appCardSettings,
            actions = actions
        )
    }
    if (dialogAppIndex == -1) {
        return
    }
    val dialogApp = apps[dialogAppIndex]
    HomeDialog(
        onDismissRequest = { dialogAppIndex = -1 },
        actionButtons = dialogApp.shortcutsList.takeIf { showShortcuts }?.map {
            it.label to { launchShortcut(it); dialogAppIndex = -1 }
        }
    ) {
        HomeDialogHeader(
            packageName = dialogApp.packageName,
            label = dialogApp.label,
            isHidden = dialogApp.isHidden,
            actions = actions,
            onDismissRequest = { dialogAppIndex = -1 }
        )
    }
}
