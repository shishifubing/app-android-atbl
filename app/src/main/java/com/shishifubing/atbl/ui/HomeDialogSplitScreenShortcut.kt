package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R
import com.shishifubing.atbl.data.HomeDialogState

private val buttons = HomeDialogState.Buttons(
    HomeDialogState.Button(
        label = R.string.drawer_app_delete_split_screen_shortcut,
        id = 0
    )
)

@Composable
fun HomeDialogSplitScreenShortcut(
    shortcut: Model.SplitScreenShortcut,
    onSplitScreenShortcutsDialogClick: (Model.SplitScreenShortcut) -> Unit,
    onHeaderAction: (Model.App, HomeDialogState.HeaderActions) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    HomeDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        actionButtons = buttons,
        onButtonClick = { onSplitScreenShortcutsDialogClick(shortcut) },
        header = HomeDialogState.Header.Shortcut(shortcut),
        onHeaderAction = onHeaderAction
    )
}