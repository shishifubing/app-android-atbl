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
    val layoutHorizontalPadding = (0..150 step 10).map { it.toString() }
    val layoutVerticalPadding = (0..150 step 10).map { it.toString() }
}

@Composable
fun SettingsGroupLayout(
    reverseOrder: Boolean,
    setReverseOrder: (Boolean) -> Unit,
    horizontalPadding: Int,
    setHorizontalPadding: (Int) -> Unit,
    verticalPadding: Int,
    setVerticalPadding: (Int) -> Unit,
    horizontalArrangement: LauncherHorizontalArrangement,
    setHorizontalArrangement: (LauncherHorizontalArrangement) -> Unit,
    verticalArrangement: LauncherVerticalArrangement,
    setVerticalArrangement: (LauncherVerticalArrangement) -> Unit,
    sortBy: LauncherSortBy,
    setSortBy: (LauncherSortBy) -> Unit
) {
    SettingsGroup(R.string.settings_group_layout) {
        LayoutReverseOrder(
            reverse = reverseOrder,
            setReverse = setReverseOrder
        )
        LayoutHorizontalPadding(
            padding = horizontalPadding,
            setPadding = setHorizontalPadding
        )
        LayoutVerticalPadding(
            padding = verticalPadding,
            setPadding = setVerticalPadding
        )
        LayoutHorizontalArrangement(
            arrangement = horizontalArrangement,
            setArrangement = setHorizontalArrangement
        )
        LayoutVerticalArrangement(
            arrangement = verticalArrangement,
            setArrangement = setVerticalArrangement
        )
        LayoutSortBy(
            sortBy = sortBy,
            setSortBy = setSortBy
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
    SettingsFieldSingleChoice(
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
    SettingsFieldSingleChoice(
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
    SettingsFieldSingleChoiceEnum(
        name = R.string.settings_layout_sort_by,
        selectedOption = sortBy,
        onConfirm = { setSortBy(it) }
    )
}

@Composable
private fun LayoutReverseOrder(
    reverse: Boolean,
    setReverse: (Boolean) -> Unit
) {
    SettingsFieldSwitch(
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
    SettingsFieldSingleChoiceEnum(
        name = R.string.settings_layout_vertical_arrangement,
        selectedOption = arrangement,
        onConfirm = setArrangement
    )
}


@Composable
private fun LayoutHorizontalArrangement(
    arrangement: LauncherHorizontalArrangement,
    setArrangement: (LauncherHorizontalArrangement) -> Unit
) {
    SettingsFieldSingleChoiceEnum(
        name = R.string.settings_layout_horizontal_arrangement,
        selectedOption = arrangement,
        onConfirm = setArrangement
    )
}