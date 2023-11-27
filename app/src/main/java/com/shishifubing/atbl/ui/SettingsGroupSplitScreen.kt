package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.R

@Composable
fun SettingsGroupSplitScreen(
    uiState: SettingsScreenUiState.Success,
    actions: SettingsActions
) {
    SettingsGroup(R.string.settings_group_split_screen) {
        SplitScreenShortcuts(
            apps = uiState.apps,
            shortcuts = uiState.splitScreenShortcuts,
            addShortcut = actions::addSplitScreenShortcut,
            removeShortcut = actions::removeSplitScreenShortcut,
            shortcutSeparator = uiState.settings.appCardSplitScreenSeparator
        )
        SplitScreenShortcutSeparator(
            separator = uiState.settings.appCardSplitScreenSeparator,
            setSeparator = {
                actions.updateSettings {
                    appCardSplitScreenSeparator = it
                }
            }
        )
    }
}

@Composable
private fun SplitScreenShortcuts(
    apps: Collection<LauncherApp>,
    shortcuts: List<LauncherSplitScreenShortcut>,
    shortcutSeparator: String,
    addShortcut: (LauncherApp, LauncherApp) -> Unit,
    removeShortcut: (LauncherSplitScreenShortcut) -> Unit,
) {
    var selectedTop by remember { mutableStateOf<LauncherApp?>(null) }
    var selectedBottom by remember { mutableStateOf<LauncherApp?>(null) }
    val sortedApps = apps.sortedBy { it.label }
    SettingsFieldCustomItemWithAdd(
        name = R.string.settings_split_screen_shortcuts,
        itemsCount = shortcuts.size,
        itemsKey = { i -> shortcuts[i].toByteArray() },
        addItemContent = {
            Column(modifier = Modifier.weight(1f)) {
                SettingsDropDownSelectApp(
                    apps = sortedApps,
                    onValueChange = { selectedTop = it }
                )
                SettingsDropDownSelectApp(
                    apps = sortedApps,
                    onValueChange = { selectedBottom = it }
                )
            }
        },
        onAddItemConfirm = {
            if (selectedTop != null && selectedBottom != null) {
                addShortcut(selectedTop!!, selectedBottom!!)
            }
        },
        onAddItemDismiss = { selectedTop = null; selectedBottom = null }
    ) { i ->
        val shortcut = shortcuts[i]
        Text(
            listOf(
                shortcut.appTop.label,
                shortcut.appBottom.label
            ).joinToString(shortcutSeparator)
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { removeShortcut(shortcut) }) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete entry",
            )
        }
    }
}

@Composable
private fun SplitScreenShortcutSeparator(
    separator: String,
    setSeparator: (String) -> Unit
) {
    SettingsFieldTextInput(
        name = R.string.settings_split_screen_shortcut_separator,
        initialValue = separator,
        onConfirm = setSeparator
    )
}