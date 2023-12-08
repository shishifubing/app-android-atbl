package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shishifubing.atbl.R
import com.shishifubing.atbl.data.HomeDialogState
import com.shishifubing.atbl.data.HomeDialogState.Buttons
import com.shishifubing.atbl.data.HomeDialogState.LauncherDialogActions.AddScreenAfter
import com.shishifubing.atbl.data.HomeDialogState.LauncherDialogActions.AddScreenBefore
import com.shishifubing.atbl.data.HomeDialogState.LauncherDialogActions.GoToAddWidget
import com.shishifubing.atbl.data.HomeDialogState.LauncherDialogActions.GoToSettings
import com.shishifubing.atbl.data.HomeDialogState.LauncherDialogActions.HideHiddenApps
import com.shishifubing.atbl.data.HomeDialogState.LauncherDialogActions.RemoveScreen
import com.shishifubing.atbl.data.HomeDialogState.LauncherDialogActions.ShowHiddenApps


private val buttons = Buttons(
    HomeDialogState.Button(
        label = R.string.launcher_dialog_settings,
        id = GoToSettings
    ),
    HomeDialogState.Button(
        label = R.string.launcher_dialog_add_widget,
        id = GoToAddWidget
    ),
    HomeDialogState.Button(
        label = R.string.launcher_dialog_hide_hidden_apps,
        id = HideHiddenApps
    ),
    HomeDialogState.Button(
        label = R.string.launcher_dialog_show_hidden_apps,
        id = ShowHiddenApps
    ),
    HomeDialogState.Button(
        label = R.string.launcher_dialog_add_screen_before,
        id = AddScreenBefore
    ),
    HomeDialogState.Button(
        label = R.string.launcher_dialog_add_screen_after,
        id = AddScreenAfter
    ),
    HomeDialogState.Button(
        label = R.string.launcher_dialog_remove_screen,
        id = RemoveScreen
    )
)

@Composable
fun HomeDialogLauncherActions(
    state: HomeDialogState.LauncherDialogState,
    onLauncherDialogAction: (
        HomeDialogState.LauncherDialogState,
        HomeDialogState.LauncherDialogActions
    ) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    HomeDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        actionButtons = buttons,
        showButton = { button ->
            when (button) {
                HideHiddenApps -> state.showHiddenApps
                ShowHiddenApps -> !state.showHiddenApps
                RemoveScreen -> state.pageCount > 1
                else -> true
            }
        },
        onButtonClick = { onLauncherDialogAction(state, it) }
    )
}