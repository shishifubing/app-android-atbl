package com.shishifubing.atbl.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import com.shishifubing.atbl.LauncherFontFamily
import com.shishifubing.atbl.LauncherHorizontalArrangement
import com.shishifubing.atbl.LauncherSettings
import com.shishifubing.atbl.LauncherSortBy
import com.shishifubing.atbl.LauncherSplitScreenShortcut
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.LauncherTextStyle
import com.shishifubing.atbl.LauncherVerticalArrangement
import com.shishifubing.atbl.R
import java.io.FileOutputStream

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
    vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val apps by vm.appsFlow.collectAsState(listOf())
    val shortcuts by vm.shortcutsFlow.collectAsState(listOf())
    val settings by vm.settingsFlow.collectAsState(vm.initialSettings)
    Column(modifier = modifier) {
        SettingsGroup(R.string.settings_group_general) {
            BackupExport(vm.settings.collectAsState())
            BackupImport(vm::updateSettingsFromBytes)
            BackupReset(vm::backupReset)
        }
        SettingsGroup(R.string.settings_group_hidden_apps) {
            HiddenApps(
                apps = apps,
                setHiddenApps = vm::setHiddenApps
            )
        }
        SettingsGroup(R.string.settings_group_split_screen) {
            SplitScreenShortcuts(
                apps = apps,
                shortcuts = shortcuts,
                addShortcut = vm::addSplitScreenShortcut,
                removeShortcut = vm::removeSplitScreenShortcut,
                settings = settings
            )
            SplitScreenShortcutSeparator(
                separator = vm.splitScreenSeparator.collectAsState(),
                setSeparator = vm.splitScreenSeparator.setter
            )
        }
        SettingsGroup(R.string.settings_group_layout) {
            LayoutReverseOrder(vm, settings)
            LayoutHorizontalPadding(vm, settings)
            LayoutVerticalPadding(vm, settings)
            LayoutHorizontalArrangement(
                arrangement = settings.appLayoutHorizontalArrangement,
                setArrangement = vm::setHorizontalArrangement
            )
            LayoutVerticalArrangement(vm, settings)
            LayoutSortBy(vm, settings)
        }
        SettingsGroup(R.string.settings_group_app_card) {
            AppCardRemoveSpaces(
                removeSpaces = vm.appCardRemoveSpaces.collectAsState(),
                setRemoveSpaces = vm.appCardRemoveSpaces.setter
            )
            AppCardLowercase(
                lowercase = settings.appCardLabelLowercase,
                setLowercase = vm::setAppCardLowercase
            )
            AppCardFontFamily(vm, settings)
            AppCardTextStyle(vm, settings)
            AppCardTextColor(
                color = settings.appCardTextColor,
                setColor = vm::setAppCardTextColor
            )
            AppCardPadding(
                padding = settings.appCardPadding,
                setPadding = vm::setAppCardPadding
            )
        }
    }
}

@Composable
fun BackupReset(
    resetSettings: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    SettingsField(
        name = R.string.settings_backup_reset,
        label = stringResource(R.string.settings_backup_reset_label),
        onClick = { showDialog = true }
    )
    if (!showDialog) {
        return
    }
    SettingsDialog(
        name = R.string.settings_backup_reset,
        onConfirm = {
            resetSettings()
            showDialog = false
        },
        onDismissRequest = { showDialog = false },
        itemsCount = 1,
        itemsKey = { 0 }
    ) {
        SettingsButton(
            text = stringResource(R.string.settings_backup_reset_confirmation),
            addButton = false
        )
    }
}

@Composable
fun BackupImport(
    updateFromBytes: (ByteArray) -> Unit
) {
    var result by remember { mutableStateOf<Uri?>(null) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            result = it
        }
    SettingsField(
        name = R.string.settings_backup_import,
        label = stringResource(R.string.settings_backup_import_label),
        onClick = { launcher.launch(arrayOf("application/*")) }
    )
    result?.let {
        LocalContext.current.contentResolver.openInputStream(it)
            ?.use { stream ->
                updateFromBytes(stream.readBytes())
            }
    }
}

@Composable
fun BackupExport(
    settings: State<LauncherSettings>
) {
    var result by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        CreateDocument("application/binpb")
    ) {
        result = it
    }
    SettingsField(
        name = R.string.settings_backup_export,
        label = stringResource(R.string.settings_backup_export_label),
        onClick = {
            launcher.launch("atbl-settings.binpb")
        }
    )
    result?.let {
        LocalContext.current.contentResolver.openFileDescriptor(it, "w")
            ?.use { file ->
                FileOutputStream(file.fileDescriptor).use { stream ->
                    settings.value.writeTo(stream)
                }
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
    apps: Collection<LauncherApp>,
    shortcuts: List<LauncherSplitScreenShortcut>,
    addShortcut: (LauncherApp, LauncherApp) -> Unit,
    removeShortcut: (LauncherSplitScreenShortcut) -> Unit,
    settings: LauncherSettings
) {
    var selectedTop by remember { mutableStateOf<LauncherApp?>(null) }
    var selectedBottom by remember { mutableStateOf<LauncherApp?>(null) }
    val sortedApps = apps.sortedBy { it.label }
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
            ).joinToString(settings.appCardSplitScreenSeparator)
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
fun SplitScreenShortcutSeparator(
    separator: State<String>,
    setSeparator: (String) -> Unit
) {
    SettingsTextInputField(
        name = R.string.settings_split_screen_shortcut_separator,
        initialValue = separator.value,
        onConfirm = setSeparator
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
    removeSpaces: State<Boolean>,
    setRemoveSpaces: (Boolean) -> Unit
) {
    SettingsSwitchField(
        name = R.string.settings_app_label_remove_spaces,
        label = R.string.settings_app_label_remove_spaces_label,
        isToggled = removeSpaces.value,
        onClick = { setRemoveSpaces(removeSpaces.value.not()) }
    )
}

@Composable
fun AppCardLowercase(
    lowercase: Boolean,
    setLowercase: (Boolean) -> Unit
) {
    SettingsSwitchField(
        name = R.string.settings_app_label_lowercase,
        label = R.string.settings_app_label_lowercase_label,
        isToggled = lowercase,
        onClick = { setLowercase(lowercase.not()) }
    )
}

@Composable
fun AppCardTextColor(
    color: LauncherTextColor,
    setColor: (LauncherTextColor) -> Unit
) {
    var curOption by remember {
        mutableIntStateOf(
            choiceOptions.textColor.indexOf(color.name)
        )
    }
    SettingsSingleChoiceField(
        name = R.string.settings_app_card_text_color,
        selectedOption = curOption,
        options = choiceOptions.textColor,
        onConfirm = { option ->
            curOption = option
            setColor(LauncherTextColor.valueOf(choiceOptions.textColor[option]))
        }
    )
}

@Composable
fun AppCardPadding(
    padding: Int,
    setPadding: (Int) -> Unit,
) {
    var curOption by remember {
        mutableIntStateOf(
            choiceOptions.appCardPadding.indexOf(padding.toString())
        )
    }
    SettingsSingleChoiceField(
        name = R.string.settings_app_card_padding,
        selectedOption = curOption,
        options = choiceOptions.appCardPadding,
        onConfirm = { choice ->
            curOption = choice
            setPadding(choiceOptions.appCardPadding[choice].toInt())
        }
    )
}

@Composable
fun LayoutHorizontalArrangement(
    arrangement: LauncherHorizontalArrangement,
    setArrangement: (LauncherHorizontalArrangement) -> Unit
) {
    var curOption by remember {
        mutableIntStateOf(choiceOptions.horizontalArrangement.indexOf(
            arrangement.name
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_layout_horizontal_arrangement,
        selectedOption = curOption,
        options = choiceOptions.horizontalArrangement,
        onConfirm = { choice ->
            curOption = choice
            setArrangement(
                LauncherHorizontalArrangement.valueOf(
                    choiceOptions.horizontalArrangement[choice]
                )
            )
        }
    )
}

@Composable
fun HiddenApps(
    apps: Collection<LauncherApp>,
    setHiddenApps: (List<String>) -> Unit
) {
    val launcherPackageName = LocalContext.current.packageName
    val options = apps
        .filter { it.packageName != launcherPackageName }
        .sortedBy { it.label }
    var hiddenApps =
        options.mapIndexedNotNull { i, app -> if (app.isHidden) i else null }

    SettingsMultiChoiceField(
        name = R.string.settings_hidden_apps,
        selectedOptions = hiddenApps,
        options = options.map { it.label },
        onConfirm = { choices ->
            setHiddenApps(choices.map { options[it].packageName })
            hiddenApps = choices
        }
    )
}


