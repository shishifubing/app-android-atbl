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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shishifubing.atbl.LauncherFontFamily
import com.shishifubing.atbl.LauncherHorizontalArrangement
import com.shishifubing.atbl.LauncherSettings
import com.shishifubing.atbl.LauncherSortBy
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.LauncherTextStyle
import com.shishifubing.atbl.LauncherVerticalArrangement
import com.shishifubing.atbl.R

private inline fun <reified E : Enum<E>> enumToList(): List<String> {
    return enumValues<E>()
        .filterNot { it.name == "UNRECOGNIZED" }
        .map { it.name }
}

private val choiceOptions = object {
    val textStyles = enumToList<LauncherTextStyle>()
    val horizontalArrangement = enumToList<LauncherHorizontalArrangement>()
    val verticalArrangement = enumToList<LauncherVerticalArrangement>()
    val textColor = enumToList<LauncherTextColor>()
    val appCardPadding = (0..30).map { it.toString() }
    val layoutHorizontalPadding = (0..150 step 10).map { it.toString() }
    val layoutVerticalPadding = (0..150 step 10).map { it.toString() }
    val fontFamilies = enumToList<LauncherFontFamily>()
    val sortBy = enumToList<LauncherSortBy>()
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    vm: SettingsViewModel = viewModel(),
) {
    val settings by vm.settingsFlow.collectAsState(vm.initialSettings)
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        AppCardRemoveSpaces(vm, settings)
        AppCardLowercase(vm, settings)
        AppCardFontFamily(vm, settings)
        AppCardTextStyle(vm, settings)
        AppCardTextColor(vm, settings)
        AppCardPadding(vm, settings)
        LayoutSortBy(vm, settings)
        LayoutReverseOrder(vm, settings)
        LayoutHorizontalPadding(vm, settings)
        LayoutVerticalPadding(vm, settings)
        LayoutHorizontalArrangement(vm, settings)
        LayoutVerticalArrangement(vm, settings)
        HiddenApps(vm)
    }
}

@Composable
fun LayoutVerticalPadding(
    vm: SettingsViewModel,
    settings: LauncherSettings
) {
    var curOption by remember {
        mutableIntStateOf(choiceOptions.layoutVerticalPadding.indexOf(
            settings.appLayoutVerticalPadding.toString()
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_layout_vertical_padding,
        selectedOption = curOption,
        options = choiceOptions.layoutVerticalPadding,
        onConfirm = { choice ->
            curOption = choice
            vm.updateSettings {
                it.setAppLayoutVerticalPadding(choiceOptions.layoutVerticalPadding[choice].toInt())
            }
        }
    )
}

@Composable
fun LayoutHorizontalPadding(
    vm: SettingsViewModel,
    settings: LauncherSettings
) {
    var curOption by remember {
        mutableIntStateOf(choiceOptions.layoutHorizontalPadding.indexOf(
            settings.appLayoutHorizontalPadding.toString()
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_layout_horizontal_padding,
        selectedOption = curOption,
        options = choiceOptions.layoutHorizontalPadding,
        onConfirm = { choice ->
            curOption = choice
            vm.updateSettings { it.setAppLayoutHorizontalPadding(choiceOptions.layoutHorizontalPadding[choice].toInt()) }
        }
    )
}

@Composable
fun LayoutSortBy(
    vm: SettingsViewModel,
    settings: LauncherSettings
) {
    var curOption by remember {
        mutableIntStateOf(choiceOptions.sortBy.indexOf(
            settings.appLayoutSortBy.name
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_layout_sort_by,
        selectedOption = curOption,
        options = choiceOptions.sortBy,
        onConfirm = { choice ->
            curOption = choice
            vm.updateSettings {
                it.setAppLayoutSortBy(
                    LauncherSortBy.valueOf(choiceOptions.sortBy[choice])
                )
            }
        }
    )
}

@Composable
fun LayoutReverseOrder(
    vm: SettingsViewModel,
    settings: LauncherSettings
) {
    SettingsSwitchField(
        name = R.string.settings_layout_reverse_order,
        label = R.string.settings_layout_reverse_order_label,
        isToggled = settings.appLayoutReverseOrder,
        onClick = {
            vm.updateSettings {
                it.setAppLayoutReverseOrder(
                    settings.appLayoutReverseOrder.not()
                )
            }
        }
    )
}

@Composable
fun LayoutVerticalArrangement(
    vm: SettingsViewModel,
    settings: LauncherSettings
) {
    var curOption by remember {
        mutableIntStateOf(choiceOptions.verticalArrangement.indexOf(
            settings.appLayoutVerticalArrangement.name
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_layout_vertical_arrangement,
        selectedOption = curOption,
        options = choiceOptions.verticalArrangement,
        onConfirm = { choice ->
            curOption = choice
            vm.updateSettings {
                it.setAppLayoutVerticalArrangement(
                    LauncherVerticalArrangement.valueOf(choiceOptions.verticalArrangement[choice])
                )
            }
        }
    )
}

@Composable
fun AppCardFontFamily(
    vm: SettingsViewModel,
    settings: LauncherSettings
) {
    var curOption by remember {
        mutableIntStateOf(choiceOptions.fontFamilies.indexOf(
            settings.appCardFontFamily.name
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_app_card_font_family,
        selectedOption = curOption,
        options = choiceOptions.fontFamilies,
        onConfirm = { choice ->
            curOption = choice
            vm.updateSettings {
                it.setAppCardFontFamily(
                    LauncherFontFamily.valueOf(choiceOptions.fontFamilies[choice])
                )
            }
        }
    )
}

@Composable
fun AppCardTextStyle(
    vm: SettingsViewModel,
    settings: LauncherSettings
) {
    var curOption by remember {
        mutableIntStateOf(choiceOptions.textStyles.indexOf(
            settings.appCardTextStyle.name
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_app_card_text_style,
        selectedOption = curOption,
        options = choiceOptions.textStyles,
        onConfirm = { choice ->
            curOption = choice
            vm.updateSettings {
                it.setAppCardTextStyle(
                    LauncherTextStyle.valueOf(choiceOptions.textStyles[choice])
                )
            }
        }
    )
}

@Composable
fun AppCardRemoveSpaces(
    vm: SettingsViewModel,
    settings: LauncherSettings
) {
    SettingsSwitchField(
        name = R.string.settings_app_label_remove_spaces,
        label = R.string.settings_app_label_remove_spaces_label,
        isToggled = settings.appCardLabelRemoveSpaces,
        onClick = {
            vm.updateSettings {
                it.setAppCardLabelRemoveSpaces(
                    settings.appCardLabelRemoveSpaces.not()
                )
            }
        }
    )
}

@Composable
fun AppCardLowercase(
    vm: SettingsViewModel,
    settings: LauncherSettings
) {
    SettingsSwitchField(
        name = R.string.settings_lowercase,
        label = R.string.settings_lowercase_label,
        isToggled = settings.appCardLabelLowercase,
        onClick = {
            vm.updateSettings {
                it.setAppCardLabelLowercase(
                    settings.appCardLabelLowercase.not()
                )
            }
        }
    )
}

@Composable
fun AppCardTextColor(
    vm: SettingsViewModel,
    settings: LauncherSettings
) {
    var curOption by remember {
        mutableIntStateOf(
            choiceOptions.textColor.indexOf(settings.appCardTextColor.name)
        )
    }
    SettingsSingleChoiceField(
        name = R.string.settings_app_card_text_color,
        selectedOption = curOption,
        options = choiceOptions.textColor,
        onConfirm = { option ->
            curOption = option
            vm.updateSettings {
                it.setAppCardTextColor(
                    LauncherTextColor.valueOf(choiceOptions.textColor[option])
                )
            }
        }
    )
}

@Composable
fun AppCardPadding(
    vm: SettingsViewModel,
    settings: LauncherSettings
) {
    var curOption by remember {
        mutableIntStateOf(
            choiceOptions.appCardPadding.indexOf(settings.appCardPadding.toString())
        )
    }
    SettingsSingleChoiceField(
        name = R.string.settings_app_card_padding,
        selectedOption = curOption,
        options = choiceOptions.appCardPadding,
        onConfirm = { choice ->
            curOption = choice
            vm.updateSettings {
                it.setAppCardPadding(choiceOptions.appCardPadding[choice].toInt())
            }
        }
    )
}

@Composable
fun LayoutHorizontalArrangement(
    vm: SettingsViewModel,
    settings: LauncherSettings
) {
    var curOption by remember {
        mutableIntStateOf(choiceOptions.horizontalArrangement.indexOf(
            settings.appLayoutHorizontalArrangement.name
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_layout_horizontal_arrangement,
        selectedOption = curOption,
        options = choiceOptions.horizontalArrangement,
        onConfirm = { choice ->
            curOption = choice
            vm.updateSettings {
                it.setAppLayoutHorizontalArrangement(
                    LauncherHorizontalArrangement.valueOf(choiceOptions.horizontalArrangement[choice])
                )
            }
        }
    )
}

@Composable
fun HiddenApps(
    vm: SettingsViewModel
) {
    val apps by vm.appsFlow.collectAsState(vm.initialApps)
    val launcherPackageName = LocalContext.current.packageName
    val options = apps.appsList
        .filter { it.packageName != launcherPackageName }
        .sortedBy { it.label }
    var hiddenApps =
        options.mapIndexedNotNull { i, app -> if (app.isHidden) i else null }

    SettingsMultiChoiceField(
        name = R.string.settings_hidden_apps,
        selectedOptions = hiddenApps,
        options = options.map { it.label },
        onConfirm = { choices ->
            vm.setHiddenApps(choices.map { options[it].packageName })
            hiddenApps = choices
        }
    )
}

@Composable
fun SettingsField(
    @StringRes name: Int,
    label: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
                    TextButton(onClick = onDismissRequest) {
                        Text(text = stringResource(R.string.settings_choice_dialog_cancel))
                    }
                    TextButton(onClick = {
                        onConfirm()
                        onDismissRequest()
                    }) {
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
