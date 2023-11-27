package com.shishifubing.atbl.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shishifubing.atbl.R

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun LauncherRow(
    rowSettings: LauncherRowSettings,
    showHiddenApps: Boolean,
    currentPage: Int,
    launcherActions: LauncherActions,
    navigate: (route: LauncherNav) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable FlowRowScope.() -> Unit
) {
    var showLauncherDialog by remember { mutableStateOf(false) }
    FlowRow(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                rowSettings.horizontalPadding.dp,
                rowSettings.verticalPadding.dp
            )
            .combinedClickable(
                onLongClick = { showLauncherDialog = true },
                onClick = {}
            ),
        horizontalArrangement = rowSettings.horizontalArrangement.toArrangement(),
        verticalArrangement = rowSettings.verticalArrangement.toArrangement(),
        content = content
    )
    if (showLauncherDialog) {
        LauncherDialogActions(
            navigate = navigate,
            showHiddenApps = showHiddenApps,
            actions = launcherActions,
            currentPage = currentPage,
            onDismissRequest = { showLauncherDialog = false }
        )
    }
}


@Composable
private fun LauncherDialogActions(
    navigate: (route: LauncherNav) -> Unit,
    showHiddenApps: Boolean,
    currentPage: Int,
    actions: LauncherActions,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    LauncherDialog(
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

