package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R
import com.shishifubing.atbl.data.UIHomeDialogActionButtons
import com.shishifubing.atbl.data.UIHomeDialogHeaders
import com.shishifubing.atbl.data.UISettingsAppCard
import com.shishifubing.atbl.data.UISplitScreenShortcut
import com.shishifubing.atbl.data.UISplitScreenShortcuts

@Composable
fun HomeItemSplitScreenShortcuts(
    shortcuts: UISplitScreenShortcuts,
    getShortcutLabel: (Model.SplitScreenShortcut) -> String,
    launchSplitScreenShortcut: (Model.SplitScreenShortcut) -> Unit,
    removeSplitScreenShortcut: (Model.SplitScreenShortcut) -> Unit,
    transformLabel: (String, Model.Settings.AppCard) -> String,
    launchAppInfo: (Model.App) -> Unit,
    launchAppUninstall: (Model.App) -> Unit,
    setIsHidden: (Model.App, Boolean) -> Unit,
    settings: UISettingsAppCard,
) {
    var dialogShortcutIndex by remember { mutableIntStateOf(-1) }
    shortcuts.model.forEachIndexed { i, shortcut ->
        HomeItemCard(
            label = getShortcutLabel(shortcut.model),
            onClick = { launchSplitScreenShortcut(shortcut.model) },
            onLongClick = { dialogShortcutIndex = i },
            settings = settings,
            transformLabel = transformLabel
        )
    }
    if (dialogShortcutIndex != -1) {
        SplitScreenShortcutDialog(
            shortcut = shortcuts.model[dialogShortcutIndex],
            removeSplitScreenShortcut = removeSplitScreenShortcut,
            onDismissRequest = { dialogShortcutIndex = -1 },
            launchAppInfo = launchAppInfo,
            launchAppUninstall = launchAppUninstall,
            setIsHidden = setIsHidden
        )
    }
}

@Composable
private fun SplitScreenShortcutDialog(
    shortcut: UISplitScreenShortcut,
    removeSplitScreenShortcut: (Model.SplitScreenShortcut) -> Unit,
    launchAppInfo: (Model.App) -> Unit,
    launchAppUninstall: (Model.App) -> Unit,
    setIsHidden: (Model.App, Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    HomeDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        actionButtons = UIHomeDialogActionButtons(listOf(
            stringResource(R.string.drawer_app_delete_split_screen_shortcut) to {
                removeSplitScreenShortcut(shortcut.model)
                onDismissRequest()
            }
        )),
        headers = UIHomeDialogHeaders(listOf(
            {
                HomeDialogHeader(
                    app = shortcut.uiAppFirst,
                    launchAppInfo = launchAppInfo,
                    launchAppUninstall = launchAppUninstall,
                    setIsHidden = setIsHidden,
                    onDismissRequest = onDismissRequest
                )
            },
            {
                HomeDialogHeader(
                    app = shortcut.uiAppSecond,
                    launchAppInfo = launchAppInfo,
                    launchAppUninstall = launchAppUninstall,
                    setIsHidden = setIsHidden,
                    onDismissRequest = onDismissRequest
                )
            }
        ))
    )
}