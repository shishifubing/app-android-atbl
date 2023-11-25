package com.shishifubing.atbl.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shishifubing.atbl.LauncherHorizontalArrangement
import com.shishifubing.atbl.LauncherSortBy
import com.shishifubing.atbl.LauncherVerticalArrangement
import com.shishifubing.atbl.R

private val choices = object {
    val verticalArrangement = enumToList<LauncherVerticalArrangement>()
    val layoutHorizontalPadding = (0..150 step 10).map { it.toString() }
    val layoutVerticalPadding = (0..150 step 10).map { it.toString() }
    val horizontalArrangement = enumToList<LauncherHorizontalArrangement>()
    val sortBy = enumToList<LauncherSortBy>()
}

@Composable
fun SettingsGroupLayout(
    uiState: SettingsScreenUiState.Success,
    actions: SettingsActions
) {
    SettingsGroup(R.string.settings_group_layout) {
        LayoutReverseOrder(
            reverse = uiState.settings.appLayoutReverseOrder,
            setReverse = {
                actions.updateSettings {
                    appLayoutReverseOrder = it
                }
            }
        )
        LayoutHorizontalPadding(
            padding = uiState.settings.appLayoutHorizontalPadding,
            setPadding = {
                actions.updateSettings {
                    appLayoutHorizontalPadding = it
                }
            }
        )
        LayoutVerticalPadding(
            padding = uiState.settings.appLayoutVerticalPadding,
            setPadding = {
                actions.updateSettings {
                    appLayoutVerticalPadding = it
                }
            }
        )
        LayoutHorizontalArrangement(
            arrangement = uiState.settings.appLayoutHorizontalArrangement,
            setArrangement = {
                actions.updateSettings {
                    appLayoutHorizontalArrangement = it
                }
            }
        )
        LayoutVerticalArrangement(
            arrangement = uiState.settings.appLayoutVerticalArrangement,
            setArrangement = {
                actions.updateSettings {
                    appLayoutVerticalArrangement = it
                }
            }
        )
        LayoutSortBy(
            sortBy = uiState.settings.appLayoutSortBy,
            setSortBy = {
                actions.updateSettings {
                    appLayoutSortBy = it
                }
            }
        )
    }
}

@Composable
private fun LayoutVerticalPadding(
    padding: Int,
    setPadding: (Int) -> Unit
) {
    var curOption by remember {
        mutableIntStateOf(choices.layoutVerticalPadding.indexOf(
            padding.toString()
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_layout_vertical_padding,
        selectedOption = curOption,
        options = choices.layoutVerticalPadding,
        onConfirm = { choice ->
            curOption = choice
            setPadding(choices.layoutVerticalPadding[choice].toInt())
        }
    )
}

@Composable
private fun LayoutHorizontalPadding(
    padding: Int,
    setPadding: (Int) -> Unit
) {
    var curOption by remember {
        mutableIntStateOf(choices.layoutHorizontalPadding.indexOf(
            padding.toString()
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_layout_horizontal_padding,
        selectedOption = curOption,
        options = choices.layoutHorizontalPadding,
        onConfirm = { choice ->
            curOption = choice
            setPadding(choices.layoutHorizontalPadding[choice].toInt())
        }
    )
}

@Composable
private fun LayoutSortBy(
    sortBy: LauncherSortBy,
    setSortBy: (LauncherSortBy) -> Unit
) {
    var curOption by remember {
        mutableIntStateOf(choices.sortBy.indexOf(
            sortBy.name
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_layout_sort_by,
        selectedOption = curOption,
        options = choices.sortBy,
        onConfirm = { choice ->
            curOption = choice
            setSortBy(LauncherSortBy.valueOf(choices.sortBy[choice]))
        }
    )
}

@Composable
private fun LayoutReverseOrder(
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
private fun LayoutVerticalArrangement(
    arrangement: LauncherVerticalArrangement,
    setArrangement: (LauncherVerticalArrangement) -> Unit
) {
    var curOption by remember {
        mutableIntStateOf(choices.verticalArrangement.indexOf(
            arrangement.name
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_layout_vertical_arrangement,
        selectedOption = curOption,
        options = choices.verticalArrangement,
        onConfirm = { choice ->
            curOption = choice
            setArrangement(LauncherVerticalArrangement.valueOf(choices.verticalArrangement[choice]))
        }
    )
}


@Composable
private fun LayoutHorizontalArrangement(
    arrangement: LauncherHorizontalArrangement,
    setArrangement: (LauncherHorizontalArrangement) -> Unit
) {
    var curOption by remember {
        mutableIntStateOf(choices.horizontalArrangement.indexOf(
            arrangement.name
        ).let { if (it != -1) it else 0 })
    }
    SettingsSingleChoiceField(
        name = R.string.settings_layout_horizontal_arrangement,
        selectedOption = curOption,
        options = choices.horizontalArrangement,
        onConfirm = { choice ->
            curOption = choice
            setArrangement(
                LauncherHorizontalArrangement.valueOf(choices.horizontalArrangement[choice])
            )
        }
    )
}