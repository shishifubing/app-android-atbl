package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shishifubing.atbl.R

@Composable
fun HomeLauncherDialogActions(
    navigate: (route: LauncherNav) -> Unit,
    showHiddenApps: Boolean,
    currentPage: Int,
    actions: LauncherActions,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    HomeDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        actionButtons = listOf(
            stringResource(R.string.launcher_dialog_settings) to {
                navigate(LauncherNav.Settings)
            },
            stringResource(R.string.launcher_dialog_add_widget) to {
                navigate(LauncherNav.AddWidget)
            },
            if (showHiddenApps) {
                stringResource(R.string.launcher_dialog_hide_hidden_apps)
            } else {
                stringResource(R.string.launcher_dialog_show_hidden_apps)
            } to { actions.setShowHiddenApps(showHiddenApps.not()) },
            stringResource(R.string.launcher_dialog_add_screen_before) to {
                actions.addScreenBefore(currentPage)
            },
            stringResource(R.string.launcher_dialog_add_screen_after) to {
                actions.addScreenAfter(currentPage)
            },
            stringResource(R.string.launcher_dialog_remove_screen) to {
                actions.removeScreen(currentPage)
            }
        )
    )
}