package com.shishifubing.atbl.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.shishifubing.atbl.R

@Composable
fun SettingsField(
    @StringRes name: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    content: @Composable RowScope.() -> Unit = {}
) {
    Surface(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(id = name),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.headlineSmall,
                )
                if (label != null) {
                    Text(
                        text = label,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            content()
        }
    }
}

@Composable
fun SettingsSwitchField(
    @StringRes name: Int,
    @StringRes label: Int,
    isToggled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SettingsField(
        name = name, label = stringResource(label),
        modifier = modifier, onClick = onClick
    ) {
        Switch(checked = isToggled, onCheckedChange = null)
    }
}

@Composable
fun SettingsTextInputField(
    @StringRes name: Int,
    initialValue: String,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }
    SettingsField(
        name = name,
        modifier = modifier,
        label = initialValue,
        onClick = { showDialog = true }
    )
    if (showDialog) {
        var input by remember { mutableStateOf(initialValue) }
        SettingsDialog(
            name = name,
            onConfirm = { onConfirm(input) },
            options = listOf("placeholder"),
            onDismissRequest = { showDialog = false },
            modifier = modifier
        ) { _, _ ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = input,
                    singleLine = true,
                    onValueChange = { input = it }
                )
            }
        }
    }
}

@Composable
fun SettingsSingleChoiceField(
    @StringRes name: Int,
    options: List<String>,
    selectedOption: Int,
    modifier: Modifier = Modifier,
    onConfirm: (Int) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    SettingsField(
        name = name,
        modifier = modifier,
        label = options[selectedOption],
        onClick = { showDialog = true }
    )
    if (showDialog) {
        SettingsSingleChoiceDialog(
            name = name,
            options = options,
            selectedOption = selectedOption,
            onConfirm = { choice ->
                showDialog = false
                onConfirm(choice)
            },
            onDismissRequest = { showDialog = false }
        )
    }
}

@Composable
fun SettingsMultiChoiceField(
    @StringRes name: Int,
    options: List<String>,
    selectedOptions: List<Int>,
    onConfirm: (List<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    SettingsField(
        name = name,
        modifier = modifier,
        label = selectedOptions.size.toString(),
        onClick = { showDialog = true }
    )
    if (showDialog) {
        SettingsMultiChoiceDialog(
            name = name,
            options = options,
            selectedOptions = selectedOptions,
            onConfirm = { choices ->
                showDialog = false
                onConfirm(choices)
            },
            onDismissRequest = { showDialog = false }
        )
    }
}

@Composable
fun SettingsSingleChoiceDialog(
    @StringRes name: Int,
    options: List<String>,
    selectedOption: Int,
    onConfirm: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var curOption by remember { mutableIntStateOf(selectedOption) }
    SettingsDialog(
        name = name,
        options = options,
        onConfirm = { onConfirm(curOption) },
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) { i, text ->
        SettingsButton(
            text = text, isSelected = i == curOption, isRadio = true
        ) {
            curOption = i
        }
    }
}

@Composable
fun SettingsMultiChoiceDialog(
    @StringRes name: Int,
    options: List<String>,
    selectedOptions: List<Int>,
    onConfirm: (List<Int>) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val curOptions = remember { selectedOptions.toMutableStateList() }
    SettingsDialog(
        name = name,
        options = options,
        onConfirm = { onConfirm(curOptions) },
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) { i, option ->
        var isSelected = curOptions.contains(i)
        SettingsButton(
            text = option, isSelected = isSelected,
            isRadio = false
        ) {
            isSelected = if (isSelected) {
                curOptions.remove(i)
                false
            } else {
                curOptions.add(i)
                true
            }
        }
    }
}


@Composable
fun SettingsDialog(
    @StringRes name: Int,
    options: List<String>,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable LazyItemScope.(Int, String) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        ElevatedCard(modifier = modifier) {
            Column {
                Text(
                    text = stringResource(name),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
                )
                LazyColumn(
                    modifier = Modifier
                        .heightIn(
                            0.dp,
                            (LocalConfiguration.current.screenHeightDp * 0.6).dp
                        )
                ) {
                    itemsIndexed(options, itemContent = itemContent)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                        onClick = onDismissRequest
                    ) {
                        Text(text = stringResource(R.string.settings_choice_dialog_cancel))
                    }
                    TextButton(
                        modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                        onClick = {
                            onConfirm()
                            onDismissRequest()
                        }
                    ) {
                        Text(text = stringResource(R.string.settings_choice_dialog_ok))
                    }
                }
            }

        }
    }
}

@Composable
fun SettingsButton(
    text: String, isSelected: Boolean, isRadio: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Surface(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier
                .padding(
                    dimensionResource(R.dimen.padding_medium),
                    dimensionResource(R.dimen.padding_small)
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRadio) {
                RadioButton(selected = isSelected, onClick = null)
            } else {
                Checkbox(checked = isSelected, onCheckedChange = null)
            }
            Text(
                text = text,
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
            )
        }
    }
}