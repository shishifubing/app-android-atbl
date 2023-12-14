package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.data.HomeDialogState

@Composable
fun HomeDialogApp(
    app: Model.App,
    showShortcuts: Boolean,
    onAppShortcutClick: (Model.AppShortcut) -> Unit,
    onHeaderAction: (Model.App, HomeDialogState.HeaderActions) -> Unit,
    onDismissRequest: () -> Unit
) {
    val header = remember(app) { HomeDialogState.Header.App(app) }
    val buttons = remember(app) {
        HomeDialogState.Buttons(app.shortcutsList.map { shortcut ->
            HomeDialogState.Button(shortcut.label, shortcut)
        })
    }
    HomeDialog(
        onDismissRequest = onDismissRequest,
        actionButtons = buttons,
        header = header,
        onHeaderAction = onHeaderAction,
        showButtons = showShortcuts,
        onButtonClick = { onAppShortcutClick(it) }
    )
}