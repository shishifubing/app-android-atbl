package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.Model.SplitScreenShortcut
import com.shishifubing.atbl.R

@Immutable
data class HomeItemSplitScreenShortcutsState(
    val shortcuts: List<SplitScreenShortcut>,
    val getLabel: (SplitScreenShortcut, Model.Settings.AppCard) -> String,
    val launchSplitScreenShortcut: (SplitScreenShortcut) -> Unit,
    val removeSplitScreenShortcut: (SplitScreenShortcut) -> Unit,
    val launchAppInfo: (Model.App) -> Unit,
    val launchAppUninstall: (Model.App) -> Unit,
    val setIsHidden: (Model.App, Boolean) -> Unit,
    val settings: Model.Settings.AppCard,
)

@Composable
fun HomeItemSplitScreenShortcuts(state: HomeItemSplitScreenShortcutsState) {
    var dialogShortcutIndex by remember { mutableIntStateOf(-1) }
    state.shortcuts.forEachIndexed { i, shortcut ->
        HomeItemCard(
            label = state.getLabel(shortcut, state.settings),
            onClick = { state.launchSplitScreenShortcut(shortcut) },
            onLongClick = { dialogShortcutIndex = i },
            settings = state.settings
        )
    }
    if (dialogShortcutIndex != -1) {
        SplitScreenShortcutDialog(
            shortcut = state.shortcuts[dialogShortcutIndex],
            removeSplitScreenShortcut = state.removeSplitScreenShortcut,
            onDismissRequest = { dialogShortcutIndex = -1 },
            launchAppInfo = state.launchAppInfo,
            launchAppUninstall = state.launchAppUninstall,
            setIsHidden = state.setIsHidden
        )
    }
}

@Composable
private fun SplitScreenShortcutDialog(
    shortcut: SplitScreenShortcut,
    removeSplitScreenShortcut: (SplitScreenShortcut) -> Unit,
    launchAppInfo: (Model.App) -> Unit,
    launchAppUninstall: (Model.App) -> Unit,
    setIsHidden: (Model.App, Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    HomeDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        actionButtons = HomeDialogButtons(listOf(
            stringResource(R.string.drawer_app_delete_split_screen_shortcut) to {
                removeSplitScreenShortcut(shortcut)
                onDismissRequest()
            }
        )),
        headers = HomeDialogHeaders(listOf(
            {
                HomeDialogHeader(
                    app = shortcut.appSecond,
                    launchAppInfo = launchAppInfo,
                    launchAppUninstall = launchAppUninstall,
                    setIsHidden = setIsHidden,
                    onDismissRequest = onDismissRequest
                )
            },
            {
                HomeDialogHeader(
                    app = shortcut.appFirst,
                    launchAppInfo = launchAppInfo,
                    launchAppUninstall = launchAppUninstall,
                    setIsHidden = setIsHidden,
                    onDismissRequest = onDismissRequest
                )
            }
        ))
    )
}