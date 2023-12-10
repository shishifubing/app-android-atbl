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
    shortcutSeparator: String,
    setSeparator: (String) -> Unit
) {
    SettingsGroup(R.string.settings_group_split_screen) {
        SplitScreenShortcutSeparator(
            separator = shortcutSeparator,
            setSeparator = setSeparator
        )
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