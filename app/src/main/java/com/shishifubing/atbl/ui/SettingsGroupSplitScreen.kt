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
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R

@Composable
fun SettingsGroupSplitScreen(
    apps: Model.Apps,
    splitScreenShortcuts: Model.SplitScreenShortcuts,
    shortcutSeparator: String,
    addShortcut: (Model.App, Model.App) -> Unit,
    removeShortcut: (Model.SplitScreenShortcut) -> Unit,
    setSeparator: (String) -> Unit
) {
    SettingsGroup(R.string.settings_group_split_screen) {
        HomeItemSplitScreenShortcuts(
            apps = apps,
            shortcuts = splitScreenShortcuts,
            addShortcut = addShortcut,
            removeShortcut = removeShortcut,
            shortcutSeparator = shortcutSeparator
        )
        SplitScreenShortcutSeparator(
            separator = shortcutSeparator,
            setSeparator = setSeparator
        )
    }
}

@Composable
private fun HomeItemSplitScreenShortcuts(
    apps: Model.Apps,
    shortcuts: Model.SplitScreenShortcuts,
    shortcutSeparator: String,
    addShortcut: (Model.App, Model.App) -> Unit,
    removeShortcut: (Model.SplitScreenShortcut) -> Unit,
) {
    var selectedTop by remember { mutableStateOf<Model.App?>(null) }
    var selectedBottom by remember { mutableStateOf<Model.App?>(null) }
    val shortcutsList = shortcuts.shortcutsMap.values.sortedBy { it.key }
    SettingsFieldCustomItemWithAdd(
        name = R.string.settings_split_screen_shortcuts,
        itemsCount = shortcuts.shortcutsMap.size,
        itemsKey = { i -> shortcutsList[i].hashCode() },
        addItemContent = {
            Column(modifier = Modifier.weight(1f)) {
                SettingsDropDownSelectApp(
                    apps = apps,
                    onValueChange = { selectedTop = it }
                )
                SettingsDropDownSelectApp(
                    apps = apps,
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
        val shortcut = shortcutsList[i]
        Text(
            text = shortcut.appSecond.label
                    + shortcutSeparator
                    + shortcut.appFirst.label
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