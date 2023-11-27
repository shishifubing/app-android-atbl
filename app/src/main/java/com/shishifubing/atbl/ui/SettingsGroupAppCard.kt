package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shishifubing.atbl.LauncherFontFamily
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.LauncherTextStyle
import com.shishifubing.atbl.R


@Composable
fun SettingsGroupAppCard(
    uiState: SettingsScreenUiState.Success,
    actions: SettingsActions
) {
    SettingsGroup(R.string.settings_group_app_card) {
        AppCardRemoveSpaces(
            removeSpaces = uiState.settings.appCardLabelRemoveSpaces,
            setRemoveSpaces = {
                actions.updateSettings {
                    appCardLabelRemoveSpaces = it
                }
            }
        )
        AppCardLowercase(
            lowercase = uiState.settings.appCardLabelLowercase,
            setLowercase = {
                actions.updateSettings {
                    appCardLabelLowercase = it
                }
            }
        )
        AppCardFontFamily(
            fontFamily = uiState.settings.appCardFontFamily,
            setFontFamily = {
                actions.updateSettings {
                    appCardFontFamily = it
                }
            }
        )
        AppCardTextStyle(
            textStyle = uiState.settings.appCardTextStyle,
            setTextStyle = {
                actions.updateSettings {
                    appCardTextStyle = it
                }
            }
        )
        AppCardTextColor(
            color = uiState.settings.appCardTextColor,
            setColor = {
                actions.updateSettings {
                    appCardTextColor = it
                }
            }
        )
        AppCardPadding(
            padding = uiState.settings.appCardPadding,
            setPadding = {
                actions.updateSettings {
                    appCardPadding = it
                }
            }
        )
    }
}

@Composable
private fun AppCardFontFamily(
    fontFamily: LauncherFontFamily,
    setFontFamily: (LauncherFontFamily) -> Unit
) {
    SettingsFieldSingleChoiceEnum(
        name = R.string.settings_app_card_font_family,
        selectedOption = fontFamily,
        onConfirm = setFontFamily
    )
}

@Composable
private fun AppCardTextStyle(
    textStyle: LauncherTextStyle,
    setTextStyle: (LauncherTextStyle) -> Unit
) {
    SettingsFieldSingleChoiceEnum(
        name = R.string.settings_app_card_text_style,
        selectedOption = textStyle,
        onConfirm = setTextStyle
    )
}

@Composable
private fun AppCardRemoveSpaces(
    removeSpaces: Boolean,
    setRemoveSpaces: (Boolean) -> Unit
) {
    SettingsFieldSwitch(
        name = R.string.settings_app_label_remove_spaces,
        label = R.string.settings_app_label_remove_spaces_label,
        isToggled = removeSpaces,
        onClick = { setRemoveSpaces(removeSpaces.not()) }
    )
}

@Composable
private fun AppCardLowercase(
    lowercase: Boolean,
    setLowercase: (Boolean) -> Unit
) {
    SettingsFieldSwitch(
        name = R.string.settings_app_label_lowercase,
        label = R.string.settings_app_label_lowercase_label,
        isToggled = lowercase,
        onClick = { setLowercase(lowercase.not()) }
    )
}

@Composable
private fun AppCardTextColor(
    color: LauncherTextColor,
    setColor: (LauncherTextColor) -> Unit
) {
    SettingsFieldSingleChoiceEnum(
        name = R.string.settings_app_card_text_color,
        selectedOption = color,
        onConfirm = setColor
    )
}

@Composable
private fun AppCardPadding(
    padding: Int,
    setPadding: (Int) -> Unit,
) {
    val options by remember { mutableStateOf((0..30).map { it.toString() }) }
    var curOption by remember { mutableIntStateOf(options.indexOf(padding.toString())) }
    SettingsFieldSingleChoice(
        name = R.string.settings_app_card_padding,
        selectedOption = if (curOption == -1) 0 else curOption,
        options = options,
        onConfirm = { curOption = it; setPadding(options[it].toInt()) }
    )
}

