package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shishifubing.atbl.R
import com.shishifubing.atbl.data.UIHomeDialogActionButtons

@Composable
fun HomeLauncherDialogActions(
    navigate: (route: LauncherNav) -> Unit,
    showHiddenApps: Boolean,
    currentPage: Int,
    setShowHiddenApps: (Boolean) -> Unit,
    addScreenBefore: (Int) -> Unit,
    addScreenAfter: (Int) -> Unit,
    removeScreen: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    HomeDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        actionButtons = UIHomeDialogActionButtons(listOf(
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
            } to { setShowHiddenApps(showHiddenApps.not()) },
            stringResource(R.string.launcher_dialog_add_screen_before) to {
                addScreenBefore(currentPage)
            },
            stringResource(R.string.launcher_dialog_add_screen_after) to {
                addScreenAfter(currentPage)
            },
            stringResource(R.string.launcher_dialog_remove_screen) to {
                removeScreen(currentPage)
            }
        ))
    )
}