package com.shishifubing.atbl.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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
    vm: SettingsViewModel = viewModel(),
) {
    val apps by vm.appsFlow.collectAsState(vm.initialApps)
    val settings by vm.settingsFlow.collectAsState(vm.initialSettings)
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        SettingsField(
            name = R.string.settings_action_reload_apps,
            onClick = { vm.reloadApps() }
        )
        HiddenApps(vm)
        SplitScreenShortcuts(vm, apps, settings)
        SplitScreenShortcutSeparator(vm, settings)
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
    }
}

@Composable
fun SplitScreenShortcuts(
    vm: SettingsViewModel,
    apps: LauncherApps,
    settings: LauncherSettings
) {
    val options = apps.splitScreenShortcutsList
    SettingsMultiChoiceField(
        name = R.string.settings_split_screen_shortcuts,
        selectedOptions = (0 until options.size).toList(),
        options = options.map {
            listOf(
                it.appTop.label,
                it.appBottom.label
            ).joinToString(settings.appCardSplitScreenSeparator)
        },
        onConfirm = { choices ->

        }
    )
}

@Composable
fun SplitScreenShortcutSeparator(
    vm: SettingsViewModel,
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


