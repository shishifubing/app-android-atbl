package com.shishifubing.atbl.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
fun SettingsRoute(
    navigate: (route: LauncherNav) -> Unit,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val uiState by vm.uiState.collectAsState()
    ErrorToast(errorFlow = vm.error)
    LauncherScaffold(screen = LauncherNav.Settings, goBack = goBack) {
        SettingsScreen(
            modifier = modifier,
            actions = vm.settingsActions,
            uiState = uiState
        )
    }
}

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
    uiState: SettingsScreenUiState,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
) {
    if (uiState !is SettingsScreenUiState.Success) {
        return
    }
    Column(modifier = modifier) {
        SettingsGroup(R.string.settings_group_general) {
            BackupExport(
                settings = uiState.settings
            )
            BackupImport(
                updateFromBytes = actions::updateSettingsFromBytes
            )
            BackupReset(
                resetSettings = actions::backupReset
            )
        }
        SettingsGroup(R.string.settings_group_hidden_apps) {
            HiddenApps(
                apps = uiState.apps,
                setHiddenApps = actions::setHiddenApps
            )
        }
        SettingsGroup(R.string.settings_group_split_screen) {
            SplitScreenShortcuts(
                apps = uiState.apps,
                shortcuts = uiState.splitScreenShortcuts,
                addShortcut = actions::addSplitScreenShortcut,
                removeShortcut = actions::removeSplitScreenShortcut,
                shortcutSeparator = uiState.settings.appCardSplitScreenSeparator
            )
            SplitScreenShortcutSeparator(
                separator = uiState.settings.appCardSplitScreenSeparator,
                setSeparator = {
                    actions.updateSettings {
                        setAppCardSplitScreenSeparator(it)
                    }
                }
            )
        }
        SettingsGroup(R.string.settings_group_layout) {
            LayoutReverseOrder(
                reverse = uiState.settings.appLayoutReverseOrder,
                setReverse = {
                    actions.updateSettings {
                        setAppLayoutReverseOrder(it)
                    }
                }
            )
            LayoutHorizontalPadding(
                padding = uiState.settings.appLayoutHorizontalPadding,
                setPadding = {
                    actions.updateSettings {
                        setAppLayoutHorizontalPadding(it)
                    }
                }
            )
            LayoutVerticalPadding(
                padding = uiState.settings.appLayoutVerticalPadding,
                setPadding = {
                    actions.updateSettings {
                        setAppLayoutVerticalPadding(it)
                    }
                }
            )
            LayoutHorizontalArrangement(
                arrangement = uiState.settings.appLayoutHorizontalArrangement,
                setArrangement = {
                    actions.updateSettings {
                        setAppLayoutHorizontalArrangement(it)
                    }
                }
            )
            LayoutVerticalArrangement(
                arrangement = uiState.settings.appLayoutVerticalArrangement,
                setArrangement = {
                    actions.updateSettings {
                        setAppLayoutVerticalArrangement(it)
                    }
                }
            )
            LayoutSortBy(
                sortBy = uiState.settings.appLayoutSortBy,
                setSortBy = {
                    actions.updateSettings {
                        setAppLayoutSortBy(it)
                    }
                }
            )
        }
        SettingsGroup(R.string.settings_group_app_card) {
            AppCardRemoveSpaces(
                removeSpaces = uiState.settings.appCardLabelRemoveSpaces,
                setRemoveSpaces = {
                    actions.updateSettings {
                        setAppCardLabelRemoveSpaces(it)
                    }
                }
            )
            AppCardLowercase(
                lowercase = uiState.settings.appCardLabelLowercase,
                setLowercase = {
                    actions.updateSettings {
                        setAppCardLabelLowercase(it)
                    }
                }
            )
            AppCardFontFamily(
                fontFamily = uiState.settings.appCardFontFamily,
                setFontFamily = {
                    actions.updateSettings {
                        setAppCardFontFamily(it)
                    }
                }
            )
            AppCardTextStyle(
                textStyle = uiState.settings.appCardTextStyle,
                setTextStyle = {
                    actions.updateSettings {
                        setAppCardTextStyle(it)
                    }
                }
            )
            AppCardTextColor(
                color = uiState.settings.appCardTextColor,
                setColor = {
                    actions.updateSettings {
                        setAppCardTextColor(it)
                    }
                }
            )
            AppCardPadding(
                padding = uiState.settings.appCardPadding,
                setPadding = {
                    actions.updateSettings {
                        setAppCardPadding(it)
                    }
                }
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
        LocalContext.current.contentResolver
            .openInputStream(it)
            ?.use { stream -> updateFromBytes(stream.readBytes()) }
        result = null
    }
}

@Composable
fun BackupExport(
    settings: LauncherSettings
) {
    var result by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        CreateDocument("application/binpb")
    ) {
        result = it
    }
    val filename = stringResource(R.string.settings_backup_export_filename)
    SettingsField(
        name = R.string.settings_backup_export,
        label = stringResource(R.string.settings_backup_export_label),
        onClick = { launcher.launch(filename) }
    )
    result?.let {
        LocalContext.current.contentResolver.openFileDescriptor(it, "w")
            ?.use { file ->
                FileOutputStream(file.fileDescriptor).use { stream ->
                    settings.writeTo(stream)
                }
            }
    }
}

@Composable
fun SplitScreenShortcuts(
    apps: Collection<LauncherApp>,
    shortcuts: List<LauncherSplitScreenShortcut>,
    shortcutSeparator: String,
    addShortcut: (LauncherApp, LauncherApp) -> Unit,
    removeShortcut: (LauncherSplitScreenShortcut) -> Unit,
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
            ).joinToString(shortcutSeparator)
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
    separator: String,
    setSeparator: (String) -> Unit
) {
    SettingsTextInputField(
        name = R.string.settings_split_screen_shortcut_separator,
        initialValue = separator,
        onConfirm = setSeparator
    )
}

@Composable
fun LayoutVerticalPadding(
    padding: Int,
    setPadding: (Int) -> Unit
) {
    var curOption by remember {
        mutableIntStateOf(choiceOptions.layoutVerticalPadding.indexOf(
            padding.toString()
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_layout_vertical_padding,
        selectedOption = curOption,
        options = choiceOptions.layoutVerticalPadding,
        onConfirm = { choice ->
            curOption = choice
            setPadding(choiceOptions.layoutVerticalPadding[choice].toInt())
        }
    )
}

@Composable
fun LayoutHorizontalPadding(
    padding: Int,
    setPadding: (Int) -> Unit
) {
    var curOption by remember {
        mutableIntStateOf(choiceOptions.layoutHorizontalPadding.indexOf(
            padding.toString()
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_layout_horizontal_padding,
        selectedOption = curOption,
        options = choiceOptions.layoutHorizontalPadding,
        onConfirm = { choice ->
            curOption = choice
            setPadding(choiceOptions.layoutHorizontalPadding[choice].toInt())
        }
    )
}

@Composable
fun LayoutSortBy(
    sortBy: LauncherSortBy,
    setSortBy: (LauncherSortBy) -> Unit
) {
    var curOption by remember {
        mutableIntStateOf(choiceOptions.sortBy.indexOf(
            sortBy.name
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_layout_sort_by,
        selectedOption = curOption,
        options = choiceOptions.sortBy,
        onConfirm = { choice ->
            curOption = choice
            setSortBy(LauncherSortBy.valueOf(choiceOptions.sortBy[choice]))
        }
    )
}

@Composable
fun LayoutReverseOrder(
    reverse: Boolean,
    setReverse: (Boolean) -> Unit
) {
    SettingsSwitchField(
        name = R.string.settings_layout_reverse_order,
        label = R.string.settings_layout_reverse_order_label,
        isToggled = reverse,
        onClick = { setReverse(reverse.not()) }
    )
}

@Composable
fun LayoutVerticalArrangement(
    arrangement: LauncherVerticalArrangement,
    setArrangement: (LauncherVerticalArrangement) -> Unit
) {
    var curOption by remember {
        mutableIntStateOf(choiceOptions.verticalArrangement.indexOf(
            arrangement.name
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_layout_vertical_arrangement,
        selectedOption = curOption,
        options = choiceOptions.verticalArrangement,
        onConfirm = { choice ->
            curOption = choice
            setArrangement(LauncherVerticalArrangement.valueOf(choiceOptions.verticalArrangement[choice]))
        }
    )
}

@Composable
fun AppCardFontFamily(
    fontFamily: LauncherFontFamily,
    setFontFamily: (LauncherFontFamily) -> Unit
) {
    var curOption by remember {
        mutableIntStateOf(choiceOptions.fontFamilies.indexOf(
            fontFamily.name
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_app_card_font_family,
        selectedOption = curOption,
        options = choiceOptions.fontFamilies,
        onConfirm = { choice ->
            curOption = choice
            setFontFamily(LauncherFontFamily.valueOf(choiceOptions.fontFamilies[choice]))
        }
    )
}

@Composable
fun AppCardTextStyle(
    textStyle: LauncherTextStyle,
    setTextStyle: (LauncherTextStyle) -> Unit
) {
    var curOption by remember {
        mutableIntStateOf(choiceOptions.textStyles.indexOf(
            textStyle.name
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_app_card_text_style,
        selectedOption = curOption,
        options = choiceOptions.textStyles,
        onConfirm = { choice ->
            curOption = choice
            setTextStyle(LauncherTextStyle.valueOf(choiceOptions.textStyles[choice]))
        }
    )
}

@Composable
fun AppCardRemoveSpaces(
    removeSpaces: Boolean,
    setRemoveSpaces: (Boolean) -> Unit
) {
    SettingsSwitchField(
        name = R.string.settings_app_label_remove_spaces,
        label = R.string.settings_app_label_remove_spaces_label,
        isToggled = removeSpaces,
        onClick = { setRemoveSpaces(removeSpaces.not()) }
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
    if (curOption == -1) {
        curOption = 0
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


