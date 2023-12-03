package com.shishifubing.atbl.ui

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.shishifubing.atbl.Model
import com.shishifubing.atbl.R
import kotlinx.coroutines.flow.StateFlow

private fun <T : Enum<*>> Class<T>.names() = enumConstants!!.mapNotNull {
    if (it.name == "UNRECOGNIZED") null else it.name
}

private fun <T : Enum<*>> Class<T>.find(name: String) = enumConstants!!.first {
    it.name == name
}

@Composable
fun ErrorToast(errorFlow: StateFlow<Throwable?>) {
    val context = LocalContext.current
    val error by errorFlow.collectAsState()
    LaunchedEffect(context, error) {
        error?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun SettingsGroup(
    @StringRes name: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
        Text(
            text = stringResource(name),
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
        )
        Surface(modifier = Modifier.fillMaxWidth()) {
            Column(content = content)
        }
    }
}

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
fun SettingsFieldSwitch(
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
fun SettingsFieldTextInput(
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
    if (!showDialog) {
        return
    }
    var input by remember { mutableStateOf(initialValue) }
    SettingsDialog(
        modifier = modifier,
        name = name,
        onConfirm = { onConfirm(input); showDialog = false },
        onDismissRequest = { showDialog = false },
        itemsCount = 1,
        itemsKey = { 1 },
    ) { _ ->
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

@Composable
fun SettingsFieldCustomItemWithAdd(
    @StringRes name: Int,
    itemsCount: Int,
    itemsKey: (Int) -> Any,
    modifier: Modifier = Modifier,
    addItemContent: @Composable RowScope.() -> Unit,
    onAddItemConfirm: () -> Unit,
    onAddItemDismiss: () -> Unit,
    itemContent: @Composable RowScope.(Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    SettingsField(
        name = name,
        modifier = modifier,
        label = itemsCount.toString(),
        onClick = { showDialog = true }
    )
    if (!showDialog) {
        return
    }
    var showAddItem by remember { mutableStateOf(false) }
    SettingsDialog(
        modifier = modifier,
        name = name,
        onConfirm = { showDialog = false },
        onDismissRequest = { showDialog = false; onAddItemDismiss() },
        showCancel = false,
        itemsCount = itemsCount + 1,
        itemsKey = { i -> if (i == itemsCount) "" else itemsKey(i) },
    ) { i ->
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
            when {
                i == itemsCount && showAddItem -> {
                    addItemContent()
                    TextButton(onClick = {
                        onAddItemDismiss()
                        showAddItem = false
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Cancel addition",
                        )
                    }
                    TextButton(onClick = {
                        onAddItemConfirm()
                        showAddItem = false
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Confirm addition",
                        )
                    }
                }

                i == itemsCount && !showAddItem ->
                    TextButton(onClick = { showAddItem = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add entry",
                        )
                    }

                else -> itemContent(i)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropDownSelectApp(
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

@Composable
fun SettingsFieldSingleChoice(
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
    if (!showDialog) {
        return
    }
    var curChoice by remember { mutableIntStateOf(selectedOption) }
    SettingsDialog(
        modifier = modifier,
        name = name,
        onConfirm = { onConfirm(curChoice); showDialog = false },
        onDismissRequest = { showDialog = false },
        itemsCount = options.size,
        itemsKey = { i -> options[i] }
    ) { i ->
        SettingsButton(
            text = options[i], isSelected = i == curChoice, isRadio = true
        ) {
            curChoice = i
        }
    }
}


@Composable
fun <T : Enum<*>> SettingsFieldSingleChoiceEnum(
    @StringRes name: Int,
    selectedOption: T,
    modifier: Modifier = Modifier,
    onConfirm: (enum: T) -> Unit,
) {
    val options by remember { mutableStateOf(selectedOption::class.java.names()) }
    SettingsFieldSingleChoice(
        modifier = modifier,
        name = name,
        options = options,
        selectedOption = options.indexOf(selectedOption.name),
        onConfirm = { i -> onConfirm(selectedOption::class.java.find(options[i])) }
    )
}

@Composable
fun <T : Enum<*>> SettingsMultiChoiceFieldEnum(
    @StringRes name: Int,
    enum: Class<T>,
    selectedOptions: List<T>,
    onConfirm: (List<T>) -> Unit,
    modifier: Modifier = Modifier
) {
    val options by remember {
        mutableStateOf(enum.names())
    }
    val selectedOptions by remember(selectedOptions) {
        mutableStateOf(selectedOptions.map { options.indexOf(it.name) })
    }
    SettingsFieldMultiChoice(
        modifier = modifier,
        name = name,
        options = options,
        selectedOptions = selectedOptions,
        onConfirm = { onConfirm(it.map { i -> enum.find(options[i]) }) }
    )
}

@Composable
fun SettingsFieldMultiChoice(
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
    if (!showDialog) {
        return
    }
    val curChoices = remember { selectedOptions.toMutableStateList() }
    SettingsDialog(
        modifier = modifier,
        name = name,
        onConfirm = { onConfirm(curChoices); showDialog = false },
        onDismissRequest = { showDialog = false },
        itemsCount = options.size,
        itemsKey = { i -> options[i] }
    ) { i ->
        var isSelected = curChoices.contains(i)
        SettingsButton(
            text = options[i], isSelected = isSelected,
            isRadio = false
        ) {
            isSelected = if (isSelected) {
                curChoices.remove(i)
                false
            } else {
                curChoices.add(i)
                true
            }
        }
    }
}

@Composable
fun SettingsDialog(
    @StringRes name: Int,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    itemsCount: Int,
    itemsKey: (Int) -> Any,
    modifier: Modifier = Modifier,
    showCancel: Boolean = true,
    itemContent: @Composable LazyItemScope.(Int) -> Unit
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
                    items(
                        count = itemsCount,
                        key = itemsKey,
                        itemContent = itemContent
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (showCancel) {
                        TextButton(
                            modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                            onClick = onDismissRequest
                        ) {
                            Text(text = stringResource(R.string.settings_choice_dialog_cancel))
                        }
                    }
                    TextButton(
                        modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                        onClick = onConfirm
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
    text: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false, isRadio: Boolean = false,
    addButton: Boolean = true, onClick: () -> Unit = { }
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
            if (addButton && isRadio) {
                RadioButton(selected = isSelected, onClick = null)
            } else if (addButton) {
                Checkbox(checked = isSelected, onCheckedChange = null)
            }
            Text(
                text = text,
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
            )
        }
    }
}