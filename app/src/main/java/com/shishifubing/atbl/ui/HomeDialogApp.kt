package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.data.HomeDialogState

@Composable
fun HomeDialogApp(
    app: Model.App,
    allShortcuts: HomeDialogState.AppShortcutButtons,
    showShortcuts: Boolean,
    onAppShortcutClick: (Model.AppShortcut) -> Unit,
    onHeaderAction: (Model.App, HomeDialogState.HeaderActions) -> Unit,
    onDismissRequest: () -> Unit
) {
    val buttons = remember(app, allShortcuts) {
        allShortcuts.buttons.getOrDefault(
            app.packageName, HomeDialogState.Buttons()
        )
    }
    HomeDialog(
        onDismissRequest = onDismissRequest,
        actionButtons = buttons,
        header = HomeDialogState.Header.App(app),
        onHeaderAction = onHeaderAction,
        showButtons = showShortcuts,
        onButtonClick = { onAppShortcutClick(it) }
    )
}