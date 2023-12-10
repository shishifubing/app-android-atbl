package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R
import com.shishifubing.atbl.data.UiState

object EditSplitScreenShortcutsRoute :
    LauncherRoute<Model.State, EditSplitScreenViewModel> {
    override val url = "edit_split_screen_shortcuts"
    override val label = R.string.navigation_edit_split_screen_shortcuts
    override val showScaffold = true

    @Composable
    override fun getViewModel(): EditSplitScreenViewModel {
        return viewModel(factory = EditSplitScreenViewModel.Factory)
    }

    @Composable
    override fun Content(
        vm: EditSplitScreenViewModel,
        uiState: UiState.Success<Model.State>
    ) {
        val state = uiState.state
        EditSplitScreenShortcutsScreen(
            shortcuts = state.splitScreenShortcuts,
            apps = state.apps,
            shortcutSeparator = state.settings.appCard.splitScreenSeparator,
            removeShortcut = vm::removeSplitScreenShortcut,
            addShortcut = vm::addSplitScreenShortcut,
        )
    }
}

@Composable
private fun EditSplitScreenShortcutsScreen(
    shortcuts: Model.SplitScreenShortcuts,
    apps: Model.Apps,
    shortcutSeparator: String,
    removeShortcut: (Model.SplitScreenShortcut) -> Unit,
    addShortcut: (Model.App, Model.App) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddItem by remember { mutableStateOf(false) }
    var selectedTop by remember { mutableStateOf<Model.App?>(null) }
    var selectedBottom by remember { mutableStateOf<Model.App?>(null) }
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        shortcuts.shortcutsMap.forEach { (_, shortcut) ->
            key(shortcut.hashCode()) {
                Row(
                    modifier = Modifier
                        .padding(
                            dimensionResource(R.dimen.padding_medium),
                            dimensionResource(R.dimen.padding_small)
                        )
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
        }
        Row(
            modifier = Modifier
                .padding(
                    dimensionResource(R.dimen.padding_medium),
                    dimensionResource(R.dimen.padding_small)
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showAddItem) {
                Column(modifier = Modifier.weight(1f)) {
                    SelectAppDropdown(
                        apps = apps,
                        onValueChange = { selectedTop = it }
                    )
                    SelectAppDropdown(
                        apps = apps,
                        onValueChange = { selectedBottom = it }
                    )
                }
                TextButton(
                    onClick = {
                        selectedTop = null
                        selectedBottom = null
                        showAddItem = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Cancel addition",
                    )
                }
                TextButton(onClick = {
                    selectedTop?.let { top ->
                        selectedBottom?.let { bottom ->
                            addShortcut(top, bottom)
                            showAddItem = false
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Confirm addition",
                    )
                }
            } else {
                TextButton(onClick = { showAddItem = true }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add entry",
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectAppDropdown(
    apps: Model.Apps,
    onValueChange: (Model.App) -> Unit
) {
    val appsSorted = apps.appsMap.values.sortedBy { it.label }
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Model.App?>(null) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selected?.label ?: "",
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            appsSorted.forEach { app ->
                key(app.hashCode()) {
                    DropdownMenuItem(
                        text = { Text(text = app.label) },
                        onClick = {
                            selected = app
                            expanded = false
                            onValueChange(app)
                        }
                    )
                }
            }
        }
    }
}
