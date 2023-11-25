package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shishifubing.atbl.LauncherFontFamily
import com.shishifubing.atbl.LauncherTextColor
import com.shishifubing.atbl.LauncherTextStyle
import com.shishifubing.atbl.R


private val choiceOptions = object {
    val textStyles = enumToList<LauncherTextStyle>()
    val textColor = enumToList<LauncherTextColor>()
    val appCardPadding = (0..30).map { it.toString() }
    val fontFamilies = enumToList<LauncherFontFamily>()
}

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
private fun AppCardTextStyle(
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
private fun AppCardRemoveSpaces(
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
private fun AppCardLowercase(
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
private fun AppCardTextColor(
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
private fun AppCardPadding(
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

