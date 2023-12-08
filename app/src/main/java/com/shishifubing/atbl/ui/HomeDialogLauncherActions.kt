package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shishifubing.atbl.R
import com.shishifubing.atbl.data.HomeDialogButtonState
import com.shishifubing.atbl.data.HomeDialogButtonsState


@Composable
fun HomeDialogLauncherActions(
    navigate: (route: LauncherNav) -> Unit,
    showHiddenApps: Boolean,
    currentPage: Int,
    pageCount: Int,
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
        actionButtons = HomeDialogButtonsState(listOf(
            HomeDialogButtonState(
                label = R.string.launcher_dialog_settings,
                onClick = { navigate(LauncherNav.Settings) },
            ),
            HomeDialogButtonState(
                label = R.string.launcher_dialog_add_widget,
                onClick = { navigate(LauncherNav.AddWidget) }
            ),
            HomeDialogButtonState(
                label = {
                    if (showHiddenApps) {
                        stringResource(R.string.launcher_dialog_hide_hidden_apps)
                    } else {
                        stringResource(R.string.launcher_dialog_show_hidden_apps)
                    }
                },
                onClick = { setShowHiddenApps(showHiddenApps.not()) }
            ),
            HomeDialogButtonState(
                label = R.string.launcher_dialog_add_screen_before,
                onClick = { addScreenBefore(currentPage) }
            ),
            HomeDialogButtonState(
                label = R.string.launcher_dialog_add_screen_after,
                onClick = { addScreenAfter(currentPage) }
            ),
            HomeDialogButtonState(
                label = R.string.launcher_dialog_remove_screen,
                onClick = { removeScreen(currentPage) },
                show = pageCount > 1
            )
        )
        )
    )
}