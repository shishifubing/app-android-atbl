package com.shishifubing.atbl.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shishifubing.atbl.LauncherApp
import com.shishifubing.atbl.LauncherApps
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
    vm: LauncherViewModel = viewModel()
) {
    val apps by vm.appsFlow.collectAsState(vm.initialApps)
    val settings by vm.settingsFlow.collectAsState(vm.initialSettings)
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        SettingsGroup(R.string.settings_group_hidden_apps) {
            HiddenApps(vm)
        }
        SettingsGroup(R.string.settings_group_split_screen) {
            SplitScreenShortcuts(vm, apps, settings)
            SplitScreenShortcutSeparator(vm, settings)
        }
        SettingsGroup(R.string.settings_group_layout) {
            LayoutReverseOrder(vm, settings)
            LayoutHorizontalPadding(vm, settings)
            LayoutVerticalPadding(vm, settings)
            LayoutHorizontalArrangement(vm, settings)
            LayoutVerticalArrangement(vm, settings)
            LayoutSortBy(vm, settings)
        }
        SettingsGroup(R.string.settings_group_app_card) {
            AppCardRemoveSpaces(vm, settings)
            AppCardLowercase(vm, settings)
            AppCardFontFamily(vm, settings)
            AppCardTextStyle(vm, settings)
            AppCardTextColor(vm, settings)
            AppCardPadding(vm, settings)
        }
    }
}

@Composable
fun SettingsGroup(
    @StringRes name: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
        Text(stringResource(name))
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
        Surface(modifier = Modifier.fillMaxWidth()) {
            Column(content = content)
        }
    }
}

@Composable
fun SplitScreenShortcuts(
    vm: LauncherViewModel,
    apps: LauncherApps,
    settings: LauncherSettings
) {
    var selectedTop by remember { mutableStateOf<LauncherApp?>(null) }
    var selectedBottom by remember { mutableStateOf<LauncherApp?>(null) }
    val shortcuts = apps.splitScreenShortcutsList
    val sortedApps = apps.appsList.sortedBy { it.label }
    SettingsCustomItemWithAddField(
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
                vm.addSplitScreenShortcut(
                    selectedTop!!, selectedBottom!!
                )
            }
        },
        onAddItemDismiss = { selectedTop = null; selectedBottom = null }
    ) { i ->
        val shortcut = shortcuts[i]
        Text(
            listOf(
                shortcut.appTop.label,
                shortcut.appBottom.label
            ).joinToString(settings.appCardSplitScreenSeparator)
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { vm.removeSplitScreenShortcut(shortcut) }) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete entry",
            )
        }
    }
}

@Composable
fun SplitScreenShortcutSeparator(
    vm: LauncherViewModel,
    settings: LauncherSettings
) {
    SettingsTextInputField(
        name = R.string.settings_split_screen_shortcut_separator,
        initialValue = settings.appCardSplitScreenSeparator,
        onConfirm = { newValue ->
            vm.updateSettings { it.setAppCardSplitScreenSeparator(newValue) }
        }
    )
}

@Composable
fun LayoutVerticalPadding(
    vm: LauncherViewModel,
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
    vm: LauncherViewModel,
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
    vm: LauncherViewModel,
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
    vm: LauncherViewModel,
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
    vm: LauncherViewModel,
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
    vm: LauncherViewModel,
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
    vm: LauncherViewModel,
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
    vm: LauncherViewModel,
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
    vm: LauncherViewModel,
    settings: LauncherSettings
) {
    SettingsSwitchField(
        name = R.string.settings_app_label_lowercase,
        label = R.string.settings_app_label_lowercase_label,
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
    vm: LauncherViewModel,
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
    vm: LauncherViewModel,
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
    vm: LauncherViewModel,
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
    vm: LauncherViewModel
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


