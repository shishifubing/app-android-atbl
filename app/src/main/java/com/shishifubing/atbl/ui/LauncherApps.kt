package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherAppShortcut

@Composable
fun LauncherApps(
    apps: List<LauncherApp>,
    actions: AppActions,
    showShortcuts: Boolean,
    launchShortcut: (LauncherAppShortcut) -> Unit,
    appCardSettings: LauncherAppCardSettings
) {
    var dialogAppIndex by remember(apps) { mutableIntStateOf(-1) }
    apps.forEachIndexed { i, app ->
        AppCard(
            label = app.label,
            onClick = { actions.launchApp(app.packageName) },
            onLongClick = { dialogAppIndex = i },
            settings = appCardSettings,
            actions = actions
        )
    }
    if (dialogAppIndex != -1) {
        val dialogApp = apps[dialogAppIndex]
        LauncherDialogApp(
            packageName = dialogApp.packageName,
            label = dialogApp.label,
            isHidden = dialogApp.isHidden,
            shortcuts = dialogApp.shortcutsList,
            actions = actions,
            launchAppShortcut = launchShortcut,
            onDismissRequest = { dialogAppIndex = -1 },
            showShortcuts = showShortcuts
        )
    }
}

@Composable
private fun LauncherDialogApp(
    packageName: String,
    label: String,
    isHidden: Boolean,
    shortcuts: List<LauncherAppShortcut>,
    actions: AppActions,
    onDismissRequest: () -> Unit,
    launchAppShortcut: (LauncherAppShortcut) -> Unit,
    showShortcuts: Boolean,
    modifier: Modifier = Modifier,
) {
    LauncherDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        actionButtons = shortcuts.takeIf { showShortcuts }?.map {
            it.label to { launchAppShortcut(it); onDismissRequest() }
        }
    ) {
        LauncherDialogHeader(
            packageName = packageName,
            label = label,
            isHidden = isHidden,
            actions = actions,
            onDismissRequest = onDismissRequest
        )
    }
}