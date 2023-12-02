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
import com.shishifubing.atbl.data.UIApp
import com.shishifubing.atbl.data.UIApps
import com.shishifubing.atbl.data.UISplitScreenShortcuts

@Composable
fun SettingsGroupSplitScreen(
    apps: UIApps,
    splitScreenShortcuts: UISplitScreenShortcuts,
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
    apps: UIApps,
    shortcuts: UISplitScreenShortcuts,
    shortcutSeparator: String,
    addShortcut: (Model.App, Model.App) -> Unit,
    removeShortcut: (Model.SplitScreenShortcut) -> Unit,
) {
    var selectedTop by remember { mutableStateOf<UIApp?>(null) }
    var selectedBottom by remember { mutableStateOf<UIApp?>(null) }
    SettingsFieldCustomItemWithAdd(
        name = R.string.settings_split_screen_shortcuts,
        itemsCount = shortcuts.model.size,
        itemsKey = { i -> shortcuts.model[i].hashCode() },
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
                addShortcut(selectedTop!!.model, selectedBottom!!.model)
            }
        },
        onAddItemDismiss = { selectedTop = null; selectedBottom = null }
    ) { i ->
        val shortcut = shortcuts.model[i]
        Text(
            listOf(
                shortcut.model.appTop.label,
                shortcut.model.appBottom.label
            ).joinToString(shortcutSeparator)
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { removeShortcut(shortcut.model) }) {
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