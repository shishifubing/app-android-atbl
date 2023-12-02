package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.data.UIApp
import com.shishifubing.atbl.data.UIApps
import com.shishifubing.atbl.data.UIHomeDialogActionButtons
import com.shishifubing.atbl.data.UIHomeDialogHeaders
import com.shishifubing.atbl.data.UISettingsAppCard

@Composable
fun HomeItemApps(
    apps: UIApps,
    launchApp: (Model.App) -> Unit,
    launchShortcut: (Model.AppShortcut) -> Unit,
    transformLabel: (String, Model.Settings.AppCard) -> String,
    launchAppInfo: (Model.App) -> Unit,
    launchAppUninstall: (Model.App) -> Unit,
    setIsHidden: (Model.App, Boolean) -> Unit,
    showShortcuts: Boolean,
    appCardSettings: UISettingsAppCard
) {
    var dialogApp by remember(apps) { mutableStateOf<UIApp?>(null) }
    apps.model.forEach { app ->
        key(app.hashCode()) {
            HomeItemCard(
                label = app.model.label,
                onClick = { launchApp(app.model) },
                onLongClick = { dialogApp = app },
                settings = appCardSettings,
                transformLabel = transformLabel
            )
        }
    }
    dialogApp?.let { app ->
        HomeDialog(
            onDismissRequest = { dialogApp = null },
            actionButtons = app.uiShortcuts.model
                .map {
                    it.model.label to {
                        launchShortcut(it.model)
                        dialogApp = app
                    }
                }
                .let { UIHomeDialogActionButtons(it) }
                .takeIf { showShortcuts }
                ?: UIHomeDialogActionButtons(listOf()),
            headers = UIHomeDialogHeaders(listOf {
                HomeDialogHeader(
                    app = app,
                    onDismissRequest = { dialogApp = null },
                    launchAppInfo = launchAppInfo,
                    launchAppUninstall = launchAppUninstall,
                    setIsHidden = setIsHidden
                )
            })
        )
    }
}
