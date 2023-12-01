package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.R

@Composable
fun HomeItemSplitScreenShortcuts(
    shortcuts: List<LauncherSplitScreenShortcut>,
    appCardSettings: LauncherAppCardSettings,
    appActions: AppActions,
    shortcutActions: SplitScreenShortcutActions,
) {
    var dialogShortcutIndex by remember { mutableIntStateOf(-1) }
    shortcuts.forEachIndexed { i, shortcut ->
        HomeItemCard(
            label = shortcut.label(appCardSettings),
            onClick = { shortcutActions.launchSplitScreenShortcut(shortcut) },
            onLongClick = { dialogShortcutIndex = i },
            settings = appCardSettings,
            actions = appActions
        )
    }
    if (dialogShortcutIndex != -1) {
        SplitScreenShortcutDialog(
            shortcut = shortcuts[dialogShortcutIndex],
            actions = appActions,
            deleteShortcut = shortcutActions::removeSplitScreenShortcut,
            onDismissRequest = { dialogShortcutIndex = -1 },
        )
    }
}

@Composable
private fun SplitScreenShortcutDialog(
    shortcut: LauncherSplitScreenShortcut,
    deleteShortcut: (LauncherSplitScreenShortcut) -> Unit,
    actions: AppActions,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    HomeDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        actionButtons = listOf(
            stringResource(R.string.drawer_app_delete_split_screen_shortcut) to {
                deleteShortcut(shortcut)
                onDismissRequest()
            }
        )
    ) {
        HomeDialogHeader(
            packageName = shortcut.appTop.packageName,
            label = shortcut.appBottom.label,
            isHidden = shortcut.appBottom.isHidden,
            actions = actions,
            onDismissRequest = onDismissRequest
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        HomeDialogHeader(
            packageName = shortcut.appBottom.packageName,
            label = shortcut.appBottom.label,
            isHidden = shortcut.appBottom.isHidden,
            actions = actions,
            onDismissRequest = onDismissRequest
        )
    }
}